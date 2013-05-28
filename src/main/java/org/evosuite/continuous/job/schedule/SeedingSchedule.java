package org.evosuite.continuous.job.schedule;

import java.util.List;

import org.evosuite.continuous.job.JobDefinition;
import org.evosuite.continuous.job.JobScheduler;

/**
 * Choose a precise order in which the CUTs will be targeted.
 * Test cases for a CUT can be used for seeding in the search
 * of the following CUTs in the schedule
 * 
 * @author arcuri
 *
 */
public class SeedingSchedule extends OneTimeSchedule{

	protected final OneTimeSchedule base;
	
	public SeedingSchedule(JobScheduler scheduler) {
		this(scheduler, new SimpleSchedule(scheduler));
	}
	
	protected SeedingSchedule(JobScheduler scheduler, OneTimeSchedule base) {
		super(scheduler);
		this.base = base;
	}
	

	@Override
	protected List<JobDefinition> createScheduleOnce() {
		List<JobDefinition> jobs = base.createScheduleOnce();
		return addDependenciesForSeeding(jobs);
	}
	
	@Override
	protected List<JobDefinition> createScheduleForWhenNotEnoughBudget(){
		/*
		 * even if we do not have enough budget to target all CUTs, we
		 * still want to use seeding.
		 */
		List<JobDefinition> jobs = super.createScheduleForWhenNotEnoughBudget(); 
		return addDependenciesForSeeding(jobs);
	}
	
	/**
	 * 
	 * @param jobs
	 * @return
	 */
	protected List<JobDefinition> addDependenciesForSeeding(List<JobDefinition> jobs){
		return jobs; // TODO
	}
	
}
