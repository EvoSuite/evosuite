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
		int totalBudget = 60 * scheduler.getTotalBudgetInMinutes() * super.getNumberOfUsableCores(); 

		int minTime = super.MINIMUM_SECONDS * data.getTotalNumberOfTestableCUTs();
		int extraTime = totalBudget - minTime;
		double timePerBranch = (double)extraTime / (double)data.getTotalNumberOfBranches(); 
		
		List<JobDefinition> jobs = new LinkedList<JobDefinition>();

		for(ClassInfo info : data.getClassInfos()){
			if(!info.isTestable()){
				continue;
			}
			/*
			 * there is a minimum that is equal to all jobs,
			 * plus extra time based on number of branches
			 */
			int budget = super.MINIMUM_SECONDS + 
					(int)(timePerBranch * info.numberOfBranches);
			JobDefinition job = new JobDefinition(
					budget, getConstantMemoryPerJob(), info.getClassName(), 0);
			jobs.add(job);
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
