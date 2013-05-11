package org.evosuite.continuous.job.schedule;

import java.util.List;

import org.evosuite.continuous.job.JobDefinition;
import org.evosuite.continuous.job.JobScheduler;

public class SimpleSchedule extends ScheduleType{

	public static final String NAME = "Simple"; 
	
	public SimpleSchedule(JobScheduler scheduler){
		super(scheduler);
	}

	@Override
	public List<JobDefinition> createNewSchedule() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean canExecuteMore() {
		// TODO Auto-generated method stub
		return false;
	}
}
