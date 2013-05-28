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
		int totalBudget = 60 * scheduler.getTotalBudgetInMinutes() * super.getNumberOfUsableCores(); 

		List<JobDefinition> jobs = new LinkedList<JobDefinition>();

		//simple case, distribute budget equally
		int budgetInSecondsPerCUT = totalBudget / data.getTotalNumberOfTestableCUTs();

		for(ClassInfo info : data.getClassInfos()){
			if(!info.isTestable()){
				continue;
			}
			JobDefinition job = new JobDefinition(
					budgetInSecondsPerCUT, getConstantMemoryPerJob(), info.getClassName(), 0);
			jobs.add(job);
		}
		return jobs;
	}

}
