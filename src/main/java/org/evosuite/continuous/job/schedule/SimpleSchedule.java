package org.evosuite.continuous.job.schedule;

import java.util.LinkedList;
import java.util.List;

import org.evosuite.continuous.job.JobDefinition;
import org.evosuite.continuous.job.JobScheduler;
import org.evosuite.continuous.project.ProjectStaticData;
import org.evosuite.continuous.project.ProjectStaticData.ClassInfo;

public class SimpleSchedule extends OneTimeSchedule{

	public SimpleSchedule(JobScheduler scheduler){
		super(scheduler);
	}

	@Override
	protected List<JobDefinition> createScheduleOnce() {

		assert enoughBudgetForAll(); 

		ProjectStaticData data = scheduler.getProjectData();
		int totalBudget = 60 * scheduler.getTotalBudgetInMinutes() * scheduler.getNumberOfUsableCores(); 

		List<JobDefinition> jobs = new LinkedList<JobDefinition>();

		//simple case, distribute budget equally
		int cores = scheduler.getNumberOfUsableCores();
		int cuts = data.getTotalNumberOfTestableCUTs();		
		int slots = (int)Math.round(cores * Math.ceil((double) cuts / (double) cores));
		int budgetInSecondsPerCUT = totalBudget / slots; 

		for(ClassInfo info : data.getClassInfos()){
			if(!info.isTestable()){
				continue;
			}
			JobDefinition job = new JobDefinition(
					budgetInSecondsPerCUT, scheduler.getConstantMemoryPerJob(), info.getClassName(), 0, null, null);
			jobs.add(job);
		}
		return jobs;
	}

}
