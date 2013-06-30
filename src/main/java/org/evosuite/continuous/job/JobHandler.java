package org.evosuite.continuous.job;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

import org.evosuite.Properties;
import org.evosuite.Properties.StoppingCondition;
import org.evosuite.continuous.persistency.StorageManager;
import org.evosuite.utils.LoggingUtils;
import org.evosuite.utils.ReportGenerator.RuntimeVariable;
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
		//TODO check Windows/Unix file paths
		cmd += " -cp " + executor.getProjectClassPath();
		
		/*
		 *  it is important to set it before calling EvoSuite, as it has to be read by Master before loading properties.
		 *  Note: the Client will get it automatically from Master
		 */
		cmd += " -D"+LoggingUtils.USE_DIFFERENT_LOGGING_XML_PARAMETER+"=logback-ctg.xml";

		StorageManager storage = executor.getStorage();
		File logs = storage.getTmpLogs(); 
		cmd += " -Devosuite.log.folder="+logs.getAbsolutePath()+"/job"+job.configurationId;
		
		/*
		 * TODO: this will likely need better handling
		 */ 
		int masterMB = 250;
		int clientMB = job.memoryInMB - masterMB;
		
		cmd += (" -Xmx" + masterMB) +"m";
		cmd += " " + org.evosuite.EvoSuite.class.getName();
		cmd += " -mem " + clientMB;
		cmd += " -class " + job.cut;
		cmd += " -Dconfiguration_id="+job.configurationId;
		
		/*
		 * TODO for now we ignore the job configuration (ie special parameter settings)
		 */
		
		/*
		 * TODO: we ll need to handle dependent CUTs for seeding,
		 * and distinguish on whether their are parent or input CUTs.
		 * Like new parameters in EvoSuite will be needed for seeding.
		 * 
		 * Furthermore, we should check on whether the dependent CUTs have been
		 * generated in this CTG run, or should rather look at previous runs.
		 * This could happen for at least 2 reasons:
		 * - under budget, and we could not run jobs for all CUTs
		 * - job for dependency crashed, but we have test cases from previous CTG run
		 * 
		 * 
		 * Regardless of whether dependencies for seeding were calculated, we might
		 * still want to use seeding based on previous CTG runs, if any test suite
		 * is available for the CUT 
		 */
		
		cmd += timeSetUp(job.seconds);
			
		File reports = storage.getTmpReports();
		File tests = storage.getTmpTests();
		
		//TODO check if it works on Windows... likely not	
		cmd += " -Dreport_dir="+reports.getAbsolutePath()+"/job"+job.configurationId;
		cmd += " -Dtest_dir="+tests.getAbsolutePath();
		
		cmd += getOutputVariables();
        
		cmd += " -Djunit_suffix="+StorageManager.junitSuffix;
		
		cmd += " -Denable_asserts_for_evosuite=false -Dsecondary_objectives=totallength -Dminimize=true  -Dtimeout=5000  "; 
        cmd += " -Dhtml=false -Dlog_timeout=false  -Dplot=false -Djunit_tests=true  -Dshow_progress=false";
        cmd += " -Dsave_all_data=false  -Dinline=false";
  		
		return cmd;
	}
	
	private String getOutputVariables(){
		//TODO add other outputs once fitness functions are fixed
		String cmd =  " -Doutput_variables=\""; 
		cmd += "TARGET_CLASS,configuration_id"; 
		cmd += RuntimeVariable.BranchCoverage+",";		
		cmd += RuntimeVariable.Minimized_Size+",";		
		cmd += RuntimeVariable.Statements_Executed+",";				
		cmd += RuntimeVariable.Total_Time+",";				
		cmd += RuntimeVariable.NumberOfGeneratedTestCases; 			
		cmd += "\"";
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
