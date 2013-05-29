package org.evosuite.continuous.job;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

import org.evosuite.continuous.persistency.StorageManager;

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

	private int timeBudgetInMinutes; 
	
	private volatile boolean executing;
	private long startTimeInMs;
	
	/**
	 * Several threads read from this queue to execute jobs
	 * on separated process
	 */
	private BlockingQueue<JobDefinition> jobQueue;
	
	/**
	 * keep track of all the jobs that have been executed so far.
	 * Each job definition (value) is indexed by its ID (key) 
	 */
	private Map<Integer,JobDefinition> finishedJobs;
	
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
			int totalMemoryInMB, int timeInMinutes){
	
		this.timeBudgetInMinutes = timeInMinutes;
		
	}

	/**
	 * Do a separate search with EvoSuite for all jobs in the given list.
	 * The executor tries a best effort to execute the jobs in the given order,
	 * although no guarantee is provided (eg, there might be dependencies among jobs).
	 * 
	 * @param jobs
	 * @throws IllegalStateException if we are already executing some jobs
	 */
	public synchronized void executeJobs(List<JobDefinition> jobs) throws IllegalStateException{
		if(executing){
			throw new IllegalStateException("Already executing jobs");
		}
		
		executing = true;
		startTimeInMs = System.currentTimeMillis(); //TODO check if make it local

		jobQueue = new ArrayBlockingQueue<JobDefinition>(1);
		finishedJobs = new ConcurrentHashMap<Integer,JobDefinition>();

		Thread[] handlers = null;
		//TODO start, and handle memory
		
		Queue<JobDefinition> toExecute = new LinkedList<JobDefinition>(); 
		List<JobDefinition> postponed = new LinkedList<JobDefinition>();
		
		try{			
			
			mainLoop: while(!toExecute.isEmpty() && !postponed.isEmpty()){
			
				long elapsed = System.currentTimeMillis() - startTimeInMs;
				if(elapsed > (timeBudgetInMinutes * 60 * 1000)){
					//TODO
				} 
				
				JobDefinition chosenJob = null;
				
				//postponed jobs have the priority
				if(!postponed.isEmpty()){
					Iterator<JobDefinition> iterator = postponed.iterator();
					postponedLoop : while(iterator.hasNext()){
						JobDefinition job = iterator.next();
						if(areDependenciesSatisfied(job)){
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
						if(areDependenciesSatisfied(job)){
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
				
				try {
					jobQueue.offer(chosenJob, 1, TimeUnit.MILLISECONDS); //TODO
				} catch (InterruptedException e) {
					//TODO stop 
					break mainLoop;
				} 
			}
			
		} finally {
			//TODO stop
			executing = false;
		}
	
	}
	
	/**
	 * Check if all jobs this one depends on are finished 
	 * 
	 * @param job
	 * @return
	 */
	private boolean areDependenciesSatisfied(JobDefinition job){
		for(Integer id : job.dependentOnIDs){
			if(!finishedJobs.containsKey(id)){
				return false;
			}
		}
		return true; 
	}
	
	
	
	public void waitForJobs(){
		//TODO
	}
}
