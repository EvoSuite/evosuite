package org.evosuite.runtime;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.TestSuiteGenerator;
import org.evosuite.ga.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.CurrentTime;
import com.examples.with.different.packagename.TimeOperation;

public class TestCurrentTime extends SystemTest {
	
	private boolean replaceCalls = Properties.REPLACE_CALLS;
	
	@Before
	public void storeValues() {
		replaceCalls = Properties.REPLACE_CALLS;
	}
	
	@After
	public void resetValues() {
		Properties.REPLACE_CALLS = replaceCalls;
	}
	
	@Test
	public void testCurrentTime1() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = CurrentTime.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.REPLACE_CALLS = true;

		String[] command = new String[] { "-generateSuite", "-class", targetClass, "-assertions" };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = (GeneticAlgorithm<?>) result;
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		Assert.assertEquals("Wrong number of goals: ", 3, goals);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
	
	@Test
	public void testTimeOperation() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = TimeOperation.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.REPLACE_CALLS = true;

		String[] command = new String[] { "-generateSuite", "-class", targetClass, "-assertions" };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = (GeneticAlgorithm<?>) result;
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		Assert.assertEquals("Wrong number of goals: ", 3, goals);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
}
