package org.evosuite.instrumentation.error;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.TestSuiteGenerator;
import org.evosuite.ga.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.errorbranch.ArrayAccess;
import com.examples.with.different.packagename.errorbranch.ArrayCreation;

public class TestArrayInstrumentation extends SystemTest {

	@Test
	public void testArrayAccessWithoutErrorBranches() {

		EvoSuite evosuite = new EvoSuite();

		String targetClass = ArrayAccess.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);

		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		Assert.assertEquals("Wrong number of goals: ", 2, goals);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}

	@Test
	public void testArrayAccessWithErrorBranches() {

		EvoSuite evosuite = new EvoSuite();

		String targetClass = ArrayAccess.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.ERROR_BRANCHES = true;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);

		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		Assert.assertEquals("Wrong number of goals: ", 8, goals);
		// One infeasible error branch
		Assert.assertEquals("Non-optimal coverage: ", 7d / 8d, best.getCoverage(), 0.001);
	}

	@Test
	public void testArrayCreationWithoutErrorBranches() {

		EvoSuite evosuite = new EvoSuite();

		String targetClass = ArrayCreation.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);

		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		Assert.assertEquals("Wrong number of goals: ", 2, goals);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}

	@Test
	public void testArrayCreationWithErrorBranches() {

		EvoSuite evosuite = new EvoSuite();

		String targetClass = ArrayCreation.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.ERROR_BRANCHES = true;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);

		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		Assert.assertEquals("Wrong number of goals: ", 4, goals);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}

}
