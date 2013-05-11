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
	
	protected ScheduleType(JobScheduler scheduler){
		this.scheduler = scheduler;
	}
	
	public abstract List<JobDefinition> createNewSchedule();
	
	public abstract boolean canExecuteMore();
	
	public  String getName(){
		try {
			return this.getClass().getField("NAME").get(null).toString();
		} catch (Exception e) {
			String msg = "Cannot access NAME field in "+this.getClass().getName()+": "+e;
			logger.error(msg,e);
			//this means a bug in EvoSuite
			throw new RuntimeException(msg);
		} 
	}
}
