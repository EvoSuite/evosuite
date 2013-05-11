package org.evosuite.continuous.job.schedule;

import java.util.List;

import org.evosuite.continuous.job.JobDefinition;
import org.evosuite.continuous.job.JobScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Note: each subclass has to define a NAME static variable
 * 
 * @author arcuri
 *
 */
public abstract class ScheduleType {

	private static Logger logger = LoggerFactory.getLogger(ScheduleType.class);
	
	protected JobScheduler scheduler;
	
	/**
	 * The minimum amount of seconds a search/job should run.
	 * Less than that, and there would be no point to even run
	 * the search.
	 */
	protected final int MINIMUM_SECONDS = 30;
	
	protected ScheduleType(JobScheduler scheduler){
		this.scheduler = scheduler;
	}
	
	public abstract List<JobDefinition> createNewSchedule() throws IllegalStateException;
	
	public abstract boolean canExecuteMore();
	
}
