package org.evosuite.continuous.job;

import java.util.List;

import org.junit.Assert;

import org.evosuite.continuous.job.JobScheduler.AvailableSchedule;
import org.evosuite.continuous.project.ProjectAnalyzer;
import org.evosuite.continuous.project.ProjectStaticData;
import org.junit.Test;

import com.examples.with.different.packagename.continuous.MoreBranches;
import com.examples.with.different.packagename.continuous.NoBranches;
import com.examples.with.different.packagename.continuous.SomeBranches;
import com.examples.with.different.packagename.continuous.SomeInterface;

public class JobSchedulerTest {
	
	@Test
	public void testBudget(){
		
		String[] cuts = new String[]{
				SomeInterface.class.getName(),
				NoBranches.class.getName(),
				SomeBranches.class.getName(),
				MoreBranches.class.getName()
		};
		
		ProjectAnalyzer analyzer = null; //TODO
		ProjectStaticData data = analyzer.analyze();
		
		int cores = 2;
		int memory = 1400;
		int budget = 2;
		
		JobScheduler scheduler = new JobScheduler(data, 
				cores, memory, budget);		
		scheduler.chooseScheduleType(AvailableSchedule.BUDGET);
		
		List<JobDefinition> jobs = scheduler.createNewSchedule();
		Assert.assertNotNull(jobs);
		//we have 4 classes, but one is an interface
		Assert.assertEquals(3, jobs.size());
		
		for(JobDefinition job : jobs){
			Assert.assertEquals(700,job.memoryInMB);
		}
		
		Assert.assertEquals(MoreBranches.class.getName(), 
				jobs.get(0).cut);
		Assert.assertEquals(SomeBranches.class.getName(), 
				jobs.get(1).cut);
		Assert.assertEquals(NoBranches.class.getName(), 
				jobs.get(2).cut);
		
		long dif01 = jobs.get(0).seconds - jobs.get(1).seconds;
		long dif12 = jobs.get(1).seconds - jobs.get(2).seconds;
		
		Assert.assertTrue(""+dif01, dif01>0);
		Assert.assertTrue(""+dif12, dif12>0);	
		
		int sum = jobs.get(0).seconds + jobs.get(1).seconds + jobs.get(2).seconds;
		Assert.assertTrue("wrong value "+sum, sum <= (cores*budget*60));
	}
	
	@Test
	public void testSimple(){
		
		String[] cuts = new String[]{
				SomeInterface.class.getName(),
				NoBranches.class.getName(),
				SomeBranches.class.getName(),
				MoreBranches.class.getName()
		};
		
		ProjectAnalyzer analyzer = null; //TODO
		ProjectStaticData data = analyzer.analyze();
		
		int cores = 2;
		int memory = 1400;
		int budget = 2;
		
		JobScheduler scheduler = new JobScheduler(data, 
				cores, memory, budget);		
		scheduler.chooseScheduleType(AvailableSchedule.SIMPLE);
		
		List<JobDefinition> jobs = scheduler.createNewSchedule();
		Assert.assertNotNull(jobs);
		//we have 4 classes, but one is an interface
		Assert.assertEquals(3, jobs.size());
		
		for(JobDefinition job : jobs){
			Assert.assertEquals(700,job.memoryInMB);
		}
		
		Assert.assertEquals(jobs.get(0).seconds, jobs.get(1).seconds);
		Assert.assertEquals(jobs.get(2).seconds, jobs.get(1).seconds);
		
		int sum = jobs.get(0).seconds + jobs.get(1).seconds + jobs.get(2).seconds;
		Assert.assertTrue("wrong value "+sum, sum <= (cores*budget*60));
	}
	
}
