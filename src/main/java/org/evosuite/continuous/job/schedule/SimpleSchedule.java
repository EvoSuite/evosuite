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

		ProjectStaticData data = scheduler.getProjectData();
		int totalBudget = 60 * scheduler.getTotalBudgetInMinutes() * super.getNumberOfUsableCores(); 
		
		List<JobDefinition> jobs = new LinkedList<JobDefinition>();
		
		if(super.enoughBudgetForAll()){
			//simple case, distribute budget equally
			int budgetPerCUT = totalBudget / data.getTotalNumberOfTestableCUTs();
			
			for(ClassInfo info : data.getClassInfos()){
				if(!info.isTestable()){
					continue;
				}
				JobDefinition job = new JobDefinition(
						budgetPerCUT, getConstantMemoryPerJob(), info.getClassName(), 0);
				jobs.add(job);
			}
			return jobs;
		} 
		
		//not enough budget
		for(ClassInfo info : data.getClassInfos()){
			if(!info.isTestable()){
				continue;
			}
			JobDefinition job = new JobDefinition(
					super.MINIMUM_SECONDS, getConstantMemoryPerJob(), info.getClassName(), 0);
			jobs.add(job);
			
			totalBudget -= super.MINIMUM_SECONDS;
			
			if(totalBudget <= 0){
				break;
			}
		}
		return jobs;
	}

}
