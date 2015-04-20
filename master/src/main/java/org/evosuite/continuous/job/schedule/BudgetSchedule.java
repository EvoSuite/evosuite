package org.evosuite.continuous.job.schedule;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.evosuite.continuous.job.JobDefinition;
import org.evosuite.continuous.job.JobScheduler;
import org.evosuite.continuous.project.ProjectStaticData;
import org.evosuite.continuous.project.ProjectStaticData.ClassInfo;

/**
 * CUTs with more branches will be given more time (ie search budget)
 * 
 * @author arcuri
 *
 */
public class BudgetSchedule extends OneTimeSchedule{

	public BudgetSchedule(JobScheduler scheduler) {
		super(scheduler);
	}

	@Override
	protected List<JobDefinition> createScheduleOnce() {
		
		ProjectStaticData data = scheduler.getProjectData();
		
		int maximumBudgetPerCore = 60 * scheduler.getConfiguration().timeInMinutes;
		
		/*
		 * the total budget we need to choose how to allocate
		 */
		int totalBudget =  maximumBudgetPerCore * scheduler.getConfiguration().getNumberOfUsableCores(); 

		/*
		 * a part of the budget is fixed, as each CUT needs a minimum
		 * of it. 
		 */
		int minTime = 60 * scheduler.getConfiguration().minMinutesPerJob * data.getTotalNumberOfTestableCUTs();
		
		/*
		 * this is what left from the minimum allocation, and that now we can
		 * choose how best to allocate
		 */
		int extraTime = totalBudget - minTime;
		
		/*
		 * check how much time we can give extra for each branch in a CUT 
		 */
		double timePerBranch = (double)extraTime / (double)data.getTotalNumberOfBranches(); 
		
		int totalLeftOver = 0;
		
		List<JobDefinition> jobs = new LinkedList<JobDefinition>();

		for(ClassInfo info : data.getClassInfos()){
			if(!info.isTestable()){
				continue;
			}
			/*
			 * there is a minimum that is equal to all jobs,
			 * plus extra time based on number of branches
			 */
			int budget = 60 * scheduler.getConfiguration().minMinutesPerJob + 
					(int)(timePerBranch * info.numberOfBranches);
			
			if(budget > maximumBudgetPerCore){
				/*
				 * Need to guarantee that no job has more than 
				 * maximumBudgetPerCore regardless of number of cores
				 */
				totalLeftOver += (budget - maximumBudgetPerCore);
				budget = maximumBudgetPerCore;
			}
			
			JobDefinition job = new JobDefinition(
					budget, scheduler.getConfiguration().getConstantMemoryPerJob(), info.getClassName(), 0, null, null);
			jobs.add(job);
			
		}
		
		if(totalLeftOver > 0){
			/*
			 * we still have some more budget to allocate
			 */
			distributeExtraBudgetEvenly(jobs,totalLeftOver,maximumBudgetPerCore);
		}
		
		/*
		 * using scheduling theory, there could be different
		 * best orders to maximize CPU usage.
		 * Here, at least for the time being, for simplicity
		 * we just try to execute the most expensive jobs
		 * as soon as possible
		 */
		
		Collections.sort(jobs, new Comparator<JobDefinition>(){
			@Override
			public int compare(JobDefinition a, JobDefinition b) {
				/*
				 * the job with takes most time will be "lower".
				 * recall that sorting is in ascending order
				 */
				return b.seconds - a.seconds;
			}
		});
		
		
		return jobs;
	}
}
