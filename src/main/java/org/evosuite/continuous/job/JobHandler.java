package org.evosuite.continuous.job;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

import org.evosuite.Properties.StoppingCondition;
import org.evosuite.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class that actually execute the job as a 
 * separated process
 * 
 * @author arcuri
 *
 */
public class JobHandler extends Thread{

	private static Logger logger = LoggerFactory.getLogger(JobHandler.class);
	
	private final JobExecutor executor;
	
	/**
	 * Main constructor
	 * 
	 * @param queue
	 */
	public JobHandler(JobExecutor executor) {
		super();
		this.executor = executor;
	}
	
	/**
	 * Return a pool of handlers, all sharing same queue and latch
	 * @param n
	 * @return
	 */
	public static JobHandler[] getPool(int n, JobExecutor executor){
		JobHandler[] jobs = new JobHandler[n];
		for(int i=0; i<jobs.length; i++){
			jobs[i] = new JobHandler(executor);
		}
		return jobs;
	}
	
	public void stopExecution(){
		this.interrupt();
	}
	
	@Override
	public void run(){
		while(!this.isInterrupted()){			
			JobDefinition job = null;

			try {
				job = executor.pollJob();			
			} catch (InterruptedException e) {
				break;
			}
			
			String command = getCommandString(job); 
			
			Process process = null;

			try{
				String base_dir = System.getProperty("user.dir");
				File dir = new File(base_dir);
				ProcessBuilder builder = new ProcessBuilder(command);
				builder.directory(dir);

				LoggingUtils.getEvoLogger().info("Going to start job for: "+job.cut);
				logger.debug("Command: "+command);
				
				process = builder.start();
				int exitCode = process.waitFor(); //no need to have timeout here, as it is handled by the scheduler/executor				
				
				if(exitCode != 0){
					logger.warn("Job ended with erroneous exit code: "+job.cut);
				}
				
			} catch (IOException e) {
				logger.error("Failed to start new job: "+e.getMessage(), e);
			} catch (InterruptedException e) {
				this.interrupt();
				if(process!=null){
					process.destroy();					
				}
			}finally {
				/*
				 * if there were problems with this job, still
				 * be sure to decrease the job counter
				 */
				executor.doneWithJob(job);
			}
		}
	}
	
	private String getCommandString(JobDefinition job){
		
		String cmd = "java "; 
		//TODO check EvoSuite CP
		cmd += " -cp " + executor.getProjectClassPath();
		
		/*
		 * TODO: this will likely need better handling
		 */ 
		int masterMB = 250;
		int clientMB = job.memoryInMB - masterMB;
		
		cmd += (" -Xmx" + masterMB) +"m";
		cmd += " " + org.evosuite.EvoSuite.class.getName();
		cmd += " -mem " + clientMB;
		cmd += " -class " + job.cut;
				
		/*
		 * TODO for now we ignore the job configuration (ie special parameter settings)
		 */
		
		/*
		 * TODO: we ll need to handle dependentOnIDs for seeding
		 */
		
		cmd += timeSetUp(job.seconds);
		
		/*
		 * TODO 
		 * - logging to file
		 * - output files
		 */
		
		return cmd;
	}
	
	private String timeSetUp(int seconds){
		/*
		 * We have at least 3 phases:
		 * - search
		 * - minimization
		 * - assertion generation
		 * 
		 * Plus extra time that is needed (eg dependency analysis)
		 * 
		 * How to best divide the budget among them?
		 * 
		 * For now we just do something very basic
		 */
		
		int search = seconds / 4;
		int minimization = seconds / 4;
		int assertions = seconds / 4 ;
		int extra = seconds / 4;
		
		if(seconds > 480) {
			minimization = 120;
			assertions = 120;
			extra = 120;
			search = seconds - 360;
		} else if(seconds > 240){
			minimization = 60;
			assertions = 60;
			extra = 60;
			search = seconds - 180;			
		}
		
		String cmd = " -Dsearch_budget="+search;
		cmd += " -Dglobal_timeout="+search;
		cmd += " -Dstopping_condition=" + StoppingCondition.MAXTIME;
		cmd += " -Dminimization_timeout="+minimization;
		cmd += " -Dassertion_timeout="+assertions;
		cmd += " -Dextra_timeout="+extra;
		
		return cmd; 
	}
}
