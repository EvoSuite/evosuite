package org.evosuite.continuous.job.schedule;

import java.util.List;

import org.evosuite.continuous.job.JobDefinition;
import org.evosuite.continuous.job.JobScheduler;

/**
 * A schedule that can only be called once.
 * In other words, the entire schedule is calculated 
 * 
 * @author arcuri
 *
 */
public abstract class OneTimeSchedule extends ScheduleType{

	private boolean called = false;
	
	public OneTimeSchedule(JobScheduler scheduler) {
		super(scheduler);		
	}
	
	@Override
	public final boolean canExecuteMore() {
		return !called;
	}

	@Override
	public final List<JobDefinition> createNewSchedule() {
		if(called){
			throw new IllegalStateException("Schedule has already been created");
		}
		
		called = true;
		
		return createScheduleOnce();
	}
	
	protected abstract List<JobDefinition> createScheduleOnce();
}
