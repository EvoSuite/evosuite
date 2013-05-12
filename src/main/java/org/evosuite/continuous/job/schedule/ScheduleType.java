package org.evosuite.continuous.job.schedule;

import java.util.List;

import org.evosuite.continuous.job.JobDefinition;
import org.evosuite.continuous.job.JobScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Root class for the different kinds of schedule
 * 
 * @author arcuri
 *
 */
public abstract class ScheduleType {

	private static Logger logger = LoggerFactory.getLogger(ScheduleType.class);
	
	protected final JobScheduler scheduler;
	
	/**
	 * The minimum amount of seconds a search/job should run.
	 * Less than that, and there would be no point to even run
	 * the search.
	 */
	protected final int MINIMUM_SECONDS = 30;
	
	protected ScheduleType(JobScheduler scheduler){
		this.scheduler = scheduler;
	}
	
	/**
	 * Create a new partial/complete schedule if there is still search budget left
	 * 
	 * @return
	 * @throws IllegalStateException
	 */
	public abstract List<JobDefinition> createNewSchedule() throws IllegalStateException;
	
	/**
	 * <p>
	 * When we get a schedule, the scheduler might decide to do not use the entire
	 * budget. Reason? It might decide to generate some test cases first, and then 
	 * use those as seeding for a new round of execution.
	 * </p>
	 * 
	 * <p>
	 * Once the budget is finished, this schedule cannot be reused. A new 
	 * instance needs to be created.
	 * </p>
	 * 
	 * @return
	 */
	public abstract boolean canExecuteMore();
	
}
