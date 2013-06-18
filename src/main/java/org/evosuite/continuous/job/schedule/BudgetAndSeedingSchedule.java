package org.evosuite.continuous.job.schedule;

import org.evosuite.continuous.job.JobScheduler;

/**
 * Combine Budget and Seeding schedules
 * 
 * @author arcuri
 *
 */
public class BudgetAndSeedingSchedule extends SeedingSchedule{

	public BudgetAndSeedingSchedule(JobScheduler scheduler) {
		super(scheduler, new BudgetSchedule(scheduler));		
	}
	
}
