package org.evosuite.continuous.job.schedule;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.evosuite.continuous.job.JobDefinition;
import org.junit.Assert;
import org.junit.Test;

public class SeedingScheduleTest {

	@Test
	public void testSortingOneDependency(){
		Set<String> dep1 = new HashSet<String>(Arrays.asList("e"));
		
		JobDefinition  a = new JobDefinition(1, 1, "a", 0, dep1, null);
		JobDefinition  b = new JobDefinition(1, 1, "b", 0, null, null);
		JobDefinition  c = new JobDefinition(1, 1, "c", 0, null, null);
		JobDefinition  d = new JobDefinition(1, 1, "d", 0, null, null);
		JobDefinition  e = new JobDefinition(1, 1, "e", 0, null, null);


		List<JobDefinition> jobs = Arrays.asList(a,b,c,d,e);
		
		jobs = SeedingSchedule.getSortedToSatisfyDependencies(jobs);
		
		Assert.assertEquals("b",jobs.get(0).cut);
		Assert.assertEquals("c",jobs.get(1).cut);
		Assert.assertEquals("d",jobs.get(2).cut);
		Assert.assertEquals("e",jobs.get(3).cut);
		Assert.assertEquals("a",jobs.get(4).cut);
	}

	@Test
	public void testSortingTwoDependencies(){
		Set<String> dep1 = new HashSet<String>(Arrays.asList("e"));
		Set<String> dep2 = new HashSet<String>(Arrays.asList("a","c"));
		
		JobDefinition  a = new JobDefinition(1, 1, "a", 0, dep1, null);
		JobDefinition  b = new JobDefinition(1, 1, "b", 0, dep2, null);
		JobDefinition  c = new JobDefinition(1, 1, "c", 0, null, null);
		JobDefinition  d = new JobDefinition(1, 1, "d", 0, null, null);
		JobDefinition  e = new JobDefinition(1, 1, "e", 0, null, null);


		List<JobDefinition> jobs = Arrays.asList(a,b,c,d,e);
		
		jobs = SeedingSchedule.getSortedToSatisfyDependencies(jobs);
		
		Assert.assertEquals("c",jobs.get(0).cut);
		Assert.assertEquals("d",jobs.get(1).cut);
		Assert.assertEquals("e",jobs.get(2).cut);
		Assert.assertEquals("a",jobs.get(3).cut);
		Assert.assertEquals("b",jobs.get(4).cut);
	}

	@Test
	public void testSortingPostponedDependencies(){
		Set<String> dep1 = new HashSet<String>(Arrays.asList("b"));
		Set<String> dep2 = new HashSet<String>(Arrays.asList("c"));
		
		JobDefinition  a = new JobDefinition(1, 1, "a", 0, dep1, null);
		JobDefinition  b = new JobDefinition(1, 1, "b", 0, dep2, null);
		JobDefinition  c = new JobDefinition(1, 1, "c", 0, null, null);


		List<JobDefinition> jobs = Arrays.asList(a,b,c);
		
		jobs = SeedingSchedule.getSortedToSatisfyDependencies(jobs);
		
		Assert.assertEquals("c",jobs.get(0).cut);
		Assert.assertEquals("b",jobs.get(1).cut);
		Assert.assertEquals("a",jobs.get(2).cut);
	}
	
}
