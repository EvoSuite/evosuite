/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.continuous.job;

import java.util.List;
import java.util.Set;

import org.evosuite.classpath.ClassPathHandler;
import org.evosuite.continuous.CtgConfiguration;
import org.evosuite.Properties.AvailableSchedule;
import org.evosuite.continuous.project.ProjectAnalyzer;
import org.evosuite.continuous.project.ProjectStaticData;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.examples.with.different.packagename.continuous.BaseForSeeding;
import com.examples.with.different.packagename.continuous.MoreBranches;
import com.examples.with.different.packagename.continuous.NoBranches;
import com.examples.with.different.packagename.continuous.OnlyAbstract;
import com.examples.with.different.packagename.continuous.OnlyAbstractImpl;
import com.examples.with.different.packagename.continuous.Simple;
import com.examples.with.different.packagename.continuous.SomeBranches;
import com.examples.with.different.packagename.continuous.SomeInterface;
import com.examples.with.different.packagename.continuous.SomeInterfaceImpl;
import com.examples.with.different.packagename.continuous.Trivial;
import com.examples.with.different.packagename.continuous.UsingSimpleAndTrivial;

public class JobSchedulerTest {

	@BeforeClass
	public static void initClass(){
		ClassPathHandler.getInstance().changeTargetCPtoTheSameAsEvoSuite();
	}
	
	@Test
	public void testBudget() {

		String[] cuts = new String[] { SomeInterface.class.getName(),
		        NoBranches.class.getName(), SomeBranches.class.getName(),
		        MoreBranches.class.getName() };

		ProjectAnalyzer analyzer = new ProjectAnalyzer(cuts);
		ProjectStaticData data = analyzer.analyze();

		int cores = 2;
		int memory = 1400;
		int budget = 2;

		CtgConfiguration conf = new CtgConfiguration(memory, cores, budget, 1, false, AvailableSchedule.BUDGET);
		
		JobScheduler scheduler = new JobScheduler(data, conf);

		List<JobDefinition> jobs = scheduler.createNewSchedule();
		Assert.assertNotNull(jobs);
		//we have 4 classes, but one is an interface
		Assert.assertEquals(3, jobs.size());

		for (JobDefinition job : jobs) {
			Assert.assertEquals(700, job.memoryInMB);
		}

		Assert.assertEquals(MoreBranches.class.getName(), jobs.get(0).cut);
		Assert.assertEquals(SomeBranches.class.getName(), jobs.get(1).cut);
		Assert.assertEquals(NoBranches.class.getName(), jobs.get(2).cut);

		long dif01 = jobs.get(0).seconds - jobs.get(1).seconds;
		long dif12 = jobs.get(1).seconds - jobs.get(2).seconds;

		Assert.assertTrue("" + dif01, dif01 > 0);
		Assert.assertTrue("" + dif12, dif12 > 0);

		int sum = jobs.get(0).seconds + jobs.get(1).seconds + jobs.get(2).seconds;
		Assert.assertTrue("wrong value " + sum, sum <= (cores * budget * 60));
	}

	@Test
	public void testNonExceedingBudget() {

		String[] cuts = new String[] { 
		        NoBranches.class.getName(), 
		        Trivial.class.getName(), 
		        MoreBranches.class.getName() };

		ProjectAnalyzer analyzer = new ProjectAnalyzer(cuts);
		ProjectStaticData data = analyzer.analyze();

		int cores = 2;
		int memory = 1400;
		int budget = 10;

		CtgConfiguration conf = new CtgConfiguration(memory, cores, budget, 1, false, AvailableSchedule.BUDGET);
		
		JobScheduler scheduler = new JobScheduler(data, conf);

		List<JobDefinition> jobs = scheduler.createNewSchedule();
		Assert.assertNotNull(jobs);
		Assert.assertEquals(3, jobs.size());

		for (JobDefinition job : jobs) {
			Assert.assertEquals(700, job.memoryInMB);
		}

		Assert.assertEquals(MoreBranches.class.getName(), jobs.get(0).cut);
		Assert.assertEquals(Trivial.class.getName(), jobs.get(1).cut);
		Assert.assertEquals(NoBranches.class.getName(), jobs.get(2).cut);

		long dif01 = jobs.get(0).seconds - jobs.get(1).seconds;
		long dif12 = jobs.get(1).seconds - jobs.get(2).seconds;

		Assert.assertTrue("" + dif01, dif01 > 0);
		Assert.assertTrue("" + dif12, dif12 > 0);

		int sum = jobs.get(0).seconds + jobs.get(1).seconds + jobs.get(2).seconds;
		Assert.assertTrue("wrong value " + sum, sum <= (cores * budget * 60));
		
		for(JobDefinition job : jobs){
			Assert.assertTrue("wrong "+job.seconds, job.seconds <= budget * 60);
		}
	}

	@Test
	public void testSimple() {

		String[] cuts = new String[] { SomeInterface.class.getName(),
		        NoBranches.class.getName(), SomeBranches.class.getName(),
		        MoreBranches.class.getName() };

		ProjectAnalyzer analyzer = new ProjectAnalyzer(cuts);
		ProjectStaticData data = analyzer.analyze();

		int cores = 2;
		int memory = 1400;
		int budget = 2;

		CtgConfiguration conf = new CtgConfiguration(memory, cores, budget, 1, false, AvailableSchedule.SIMPLE);
		JobScheduler scheduler = new JobScheduler(data, conf);
		

		List<JobDefinition> jobs = scheduler.createNewSchedule();
		Assert.assertNotNull(jobs);
		//we have 4 classes, but one is an interface
		Assert.assertEquals(3, jobs.size());

		for (JobDefinition job : jobs) {
			Assert.assertEquals(700, job.memoryInMB);
		}

		Assert.assertEquals(jobs.get(0).seconds, jobs.get(1).seconds);
		Assert.assertEquals(jobs.get(2).seconds, jobs.get(1).seconds);

		int sum = jobs.get(0).seconds + jobs.get(1).seconds + jobs.get(2).seconds;
		Assert.assertTrue("wrong value " + sum, sum <= (cores * budget * 60));
	}

	@Test
	public void testSeeding() {

		String[] cuts = new String[] { BaseForSeeding.class.getName(),
		        NoBranches.class.getName(), MoreBranches.class.getName(),
		        SomeInterface.class.getName(), SomeInterfaceImpl.class.getName(),
		        SomeBranches.class.getName(), OnlyAbstract.class.getName(),
		        OnlyAbstractImpl.class.getName(), Trivial.class.getName() };

		ProjectAnalyzer analyzer = new ProjectAnalyzer(cuts);
		ProjectStaticData data = analyzer.analyze();

		int cores = 3;
		int memory = 1800;
		int budget = 3;

		CtgConfiguration conf = new CtgConfiguration(memory, cores, budget, 1, false, AvailableSchedule.SEEDING);
		JobScheduler scheduler = new JobScheduler(data, conf);

		List<JobDefinition> jobs = scheduler.createNewSchedule();
		Assert.assertNotNull(jobs);

		for (JobDefinition job : jobs) {
			Assert.assertEquals(600, job.memoryInMB);
		}

		/*
		 * FIXME: in the long run, abstract class with no code should be skipped.
		 * at the moment, they are not, because there is default constructor that
		 * is automatically added
		 */
		
		//we have 9 classes, but 2 have no code
		Assert.assertEquals("Wrong number of jobs: " + jobs.toString(), 8, jobs.size()); //FIXME should be 7

		JobDefinition seeding = null;
		for (JobDefinition job : jobs) {
			if (job.cut.equals(BaseForSeeding.class.getName())) {
				seeding = job;
				break;
			}
		}
		Assert.assertNotNull(seeding);

		Set<String> in = seeding.inputClasses;
		Assert.assertNotNull(in);
		System.out.println(in.toString());
		Assert.assertTrue(in.contains(NoBranches.class.getName()));
		Assert.assertTrue(in.contains(SomeBranches.class.getName()));
		Assert.assertTrue(in.contains(SomeInterfaceImpl.class.getName()));
		Assert.assertTrue(in.contains(OnlyAbstractImpl.class.getName()));
		Assert.assertEquals(5, in.size()); //FIXME should be 4
	}
	
	@Test
	public void testSeedingOrder() {

		String[] cuts = new String[] { 
				Simple.class.getName(),
		        UsingSimpleAndTrivial.class.getName(), 
		        Trivial.class.getName(),
		        };

		ProjectAnalyzer analyzer = new ProjectAnalyzer(cuts);
		ProjectStaticData data = analyzer.analyze();

		int cores = 3;
		int memory = 1800;
		int budget = 2;

		CtgConfiguration conf = new CtgConfiguration(memory, cores, budget, 1, false, AvailableSchedule.SEEDING);
		JobScheduler scheduler = new JobScheduler(data, conf);

		List<JobDefinition> jobs = scheduler.createNewSchedule();
		Assert.assertNotNull(jobs);

		Assert.assertEquals("Wrong number of jobs: " + jobs.toString(), 3, jobs.size());

		//UsingSimpleAndTrivial should be the last in the schedule, as it depends on the first 2
		JobDefinition seeding = jobs.get(2);
		Assert.assertNotNull(seeding);
		Assert.assertEquals(UsingSimpleAndTrivial.class.getName(), seeding.cut);
		
		Set<String> in = seeding.inputClasses;
		Assert.assertNotNull(in);
		System.out.println(in.toString());
		Assert.assertTrue(in.contains(Simple.class.getName()));
		Assert.assertTrue(in.contains(Trivial.class.getName()));
		Assert.assertEquals(2, in.size());
	}


	@Test
	public void testSeedingAndBudget() {

		String[] cuts = new String[] { 
		        Trivial.class.getName(),
		        UsingSimpleAndTrivial.class.getName(), 
				Simple.class.getName(),
		        };

		ProjectAnalyzer analyzer = new ProjectAnalyzer(cuts);
		ProjectStaticData data = analyzer.analyze();

		int cores = 2;
		int memory = 1800;
		int budget = 3;

		CtgConfiguration conf = new CtgConfiguration(memory, cores, budget, 1, false, AvailableSchedule.BUDGET_AND_SEEDING);
		JobScheduler scheduler = new JobScheduler(data, conf);

		List<JobDefinition> jobs = scheduler.createNewSchedule();
		Assert.assertNotNull(jobs);

		Assert.assertEquals("Wrong number of jobs: " + jobs.toString(), 3, jobs.size());

		//UsingSimpleAndTrivial should be the last in the schedule, as it depends on the other 2
		JobDefinition seeding = jobs.get(2);
		Assert.assertNotNull(seeding);
		Assert.assertEquals(UsingSimpleAndTrivial.class.getName(), seeding.cut);
		
		Set<String> in = seeding.inputClasses;
		Assert.assertNotNull(in);
		System.out.println(in.toString());
		Assert.assertTrue(in.contains(Simple.class.getName()));
		Assert.assertTrue(in.contains(Trivial.class.getName()));
		Assert.assertEquals(2, in.size());
		
		
		JobDefinition simple = jobs.get(0); //should be the first, as it has the highest number of branches among the jobs with no depencencies
		Assert.assertNotNull(simple);
		Assert.assertEquals(Simple.class.getName(), simple.cut);
		
		int simpleTime = jobs.get(0).seconds;
		int trivialTime = jobs.get(1).seconds;
		int seedingTime = jobs.get(2).seconds;
		
		System.out.println("Ordered times: "+simpleTime+", "+trivialTime+", "+seedingTime);
		
		Assert.assertTrue(simpleTime > trivialTime);
		Assert.assertTrue(simpleTime < seedingTime);  //seeding, even if last, it should have more time, as it has most branches
		Assert.assertTrue(trivialTime < seedingTime);
	}


}
