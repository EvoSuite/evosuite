package org.evosuite.continuous.job;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.evosuite.continuous.persistency.StorageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Job executor will run EvoSuite on separate processes.
 * There will be no communication with these masters/clients, whose
 * visible side-effects would only be files written on local disk.
 * This does simplify the architecture a lot, especially considering we can 
 * have several instances running in parallel.
 * Furthermore, writing to disk at each search has benefit that we can recover from
 * premature crashes, reboot of machine, etc.
 * This is particularly useful considering that CTG can be left running for hours, if not
 * even days. 
 * Downside is not a big deal, as the searches in a schedule are anyway run independently. 
 * 
 * 
 * <p>
 * Note: under no case ever two different jobs should access the same files at the same time, even
 * if just for reading. Piece-of-crap OS like Windows do put locks on files based
 * on processes accessing them... for multi-process applications running on same host,
 * that is a recipe for disaster... 
 * 
 * @author arcuri
 *
 */
public class JobExecutor {

	private static Logger logger = LoggerFactory.getLogger(JobExecutor.class);

	private int timeBudgetInMinutes; 

	private volatile boolean executing;
	private long startTimeInMs;

	/**
	 * This used to wait till all jobs are finished running
	 */
	private volatile CountDownLatch latch;

	/**
	 * Several threads read from this queue to execute jobs
	 * on separated process
	 */
	private BlockingQueue<JobDefinition> jobQueue;

	/**
	 * keep track of all the jobs that have been executed so far.
	 * Each job definition (value) is indexed by the CUT name (key).
	 * This assumes in a schedule that the CUT names are unique, ie,
	 * no more than one job should exist for the same CUT 
	 */
	private Map<String,JobDefinition> finishedJobs; 

	private int numberOfCores;
	
	private String projectClassPath;
	
	private StorageManager storage;
	
	/**
	 * Main constructor
	 * 
	 * @param storage
	 * @param projectClassPath
	 * @param numberOfCores
	 * @param totalMemoryInMB
	 * @param timeInMinutes
	 */
	public JobExecutor(StorageManager storage, 
			String projectClassPath,int numberOfCores,
			int totalMemoryInMB, int timeInMinutes) throws IllegalArgumentException{

		this.storage = storage;
		if(storage.getTmpFolder() == null){
			throw new IllegalArgumentException("Storage is not initalized");
		}
		
		this.timeBudgetInMinutes = timeInMinutes;
		this.numberOfCores = numberOfCores;
		this.projectClassPath = projectClassPath;
	}

	private long getRemainingTime(){
		long elapsed = System.currentTimeMillis() - startTimeInMs;
		long budgetInMs = timeBudgetInMinutes * 60 * 1000;
		long remaining = budgetInMs - elapsed;
		return remaining;
	}
	
	/**
	 * Do a separate search with EvoSuite for all jobs in the given list.
	 * The executor tries a best effort to execute the jobs in the given order,
	 * although no guarantee is provided (eg, there might be dependencies among jobs).
	 * 
	 * @param jobs
	 * @throws IllegalStateException if we are already executing some jobs
	 */
	public synchronized void executeJobs(final List<JobDefinition> jobs) throws IllegalStateException{
		if(executing){
			throw new IllegalStateException("Already executing jobs");
		}

		logger.info("Going to execute "+jobs.size()+" jobs");
		
		executing = true;
		startTimeInMs = System.currentTimeMillis(); 		
		latch = new CountDownLatch(jobs.size());
		
		Thread mainThread = new Thread(){
			@Override
			public void run(){
				
				/*
				 * there is a good reason to have a blocking queue of size 1.
				 * we want to put jobs on the queue only when we know there is going
				 * to be a handler that can pull it.
				 * this helps the scheduler, as we can wait longer before making the decision
				 * of what job to schedule next
				 */
				jobQueue = new ArrayBlockingQueue<JobDefinition>(1);
				finishedJobs = new ConcurrentHashMap<String,JobDefinition>();				
				
				JobHandler[] handlers = JobHandler.getPool(numberOfCores,JobExecutor.this);
				for(JobHandler handler : handlers){
					handler.start();
				}
				//TODO handle memory

				Queue<JobDefinition> toExecute = new LinkedList<JobDefinition>();
				toExecute.addAll(jobs);
				
				List<JobDefinition> postponed = new LinkedList<JobDefinition>();

				long longestJob = -1l;
				
				try{			

					mainLoop: while(!toExecute.isEmpty() || !postponed.isEmpty()){

						long remaining = getRemainingTime();
						if(remaining <= 0){
							//time is over. do not submit any more job
							break mainLoop;
						} 
						
						JobDefinition chosenJob = null;

						//postponed jobs have the priority
						if(!postponed.isEmpty()){
							Iterator<JobDefinition> iterator = postponed.iterator();
							postponedLoop : while(iterator.hasNext()){
								JobDefinition job = iterator.next();
								if(areDependenciesSatisfied(jobs,job)){
									chosenJob = job;							
									iterator.remove();
									break postponedLoop;
								}
							}
						}

						if(chosenJob == null && toExecute.isEmpty()){
							assert !postponed.isEmpty();
							/*
							 * tricky case: the are no more jobs in the queue, and there are
							 * jobs that have been postponed. But, at the moment, we shouldn't 
							 * execute them due to missing dependencies.
							 * 
							 * As the dependencies are just "optimizations" (eg, seeding), it is not
							 * wrong to execute any of those postponed jobs.
							 * There might be useful heuristics to pick up one in a smart way but,
							 * for now, we just choose the first (and so oldest)
							 */
							chosenJob = postponed.get(0); //it is important to get the oldest jobs
							postponed.remove((int) 0);
						}

						if(chosenJob == null){
							assert !toExecute.isEmpty();

							toExecuteLoop : while(!toExecute.isEmpty()){
								JobDefinition job = toExecute.poll();
								if(areDependenciesSatisfied(jobs,job)){
									chosenJob = job;
									break toExecuteLoop;
								}  else {
									postponed.add(job);
								}
							}

							if(chosenJob == null){
								/*
								 * yet another tricky situation: we started with a list of jobs to execute,
								 * and none in the postponed list; but we cannot execute any of the those
								 * jobs (this could happen if they all depend on jobs that are currently running).
								 * We should hence just choose one of them. Easiest thing, and most clean,
								 * to do is just to go back to beginning of the loop
								 */
								assert !postponed.isEmpty() && toExecute.isEmpty();
								continue mainLoop;
							}
						}

						assert chosenJob != null;
						longestJob = Math.max(longestJob, chosenJob.seconds * 1000);
						
						try {
							jobQueue.offer(chosenJob, remaining, TimeUnit.MILLISECONDS); 
						} catch (InterruptedException e) {
							this.interrupt(); //important for check later
							break mainLoop;
						} 
					}

				} finally {
					/*
					 * When we arrive here, in the worst case each handler is still executing a job,
					 * plus one in the queue.
					 * Note: this check is not precise
					 */
					if(!this.isInterrupted() && longestJob > 0){				
						try {
							latch.await((longestJob*2) + (60000),TimeUnit.MILLISECONDS);
						} catch (InterruptedException e) {
							this.interrupt();
						}
					}
					
					//be sure to release the latch
					long stillNotFinished = latch.getCount();
					for(int i=0; i<stillNotFinished; i++){
						latch.countDown();
					}
					
					for(JobHandler handler : handlers){
						handler.stopExecution();
					}
					
					executing = false;
				}
			} //end of "run"
		};
		mainThread.start();
	}

	private boolean inTheSchedule(List<JobDefinition> jobs, String cut){
		for(JobDefinition job : jobs){
			if(job.cut.equals(cut)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Check if all jobs this one depends on are finished 
	 * 
	 * @param job
	 * @return
	 */
	private boolean areDependenciesSatisfied(List<JobDefinition> schedule, JobDefinition job){
		
		if(job.dependentOnClasses == null){
			return true; // no dependencies to satisfy
		}
		
		for(String name : job.dependentOnClasses){
			/*
			 * It could happen that a schedule is not complete, in the sense that
			 * we do not create jobs for each single CUT in the project.
			 * If A depends on B, but we have no job for B, then no point in postponing
			 * a job for A
			 */
			if(!inTheSchedule(schedule,name)){
				continue;
			}
			if(!finishedJobs.containsKey(name)){
				return false;
			}
		}
		return true; 
	}

	public JobDefinition pollJob() throws InterruptedException{
		return jobQueue.take();
	}
	
	public void doneWithJob(JobDefinition job){
		finishedJobs.put(job.cut, job);
		latch.countDown();		
	}
	
	public void waitForJobs() {
		/*
		 * Note: this method could be called a long while after the starting
		 * of the execution. But likely not so important to handle such situation.
		 * 
		 * Furthermore, due to crashes and phases that could end earlier (eg, minimization
		 * and assertion generation), it is likely that jobs will finish before the expected time.
		 */
		try {
			//add one extra minute just to be sure
			latch.await(timeBudgetInMinutes+1, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
		}
	}

	public String getProjectClassPath() {
		return projectClassPath;
	}

	public StorageManager getStorage() {
		return storage;
	}
}
