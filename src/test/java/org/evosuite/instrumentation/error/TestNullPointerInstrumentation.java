package org.evosuite.instrumentation.error;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.TestSuiteGenerator;
import org.evosuite.ga.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.errorbranch.Fieldaccess;
import com.examples.with.different.packagename.errorbranch.Methodcall;

public class TestNullPointerInstrumentation extends SystemTest {

	@Test
	public void testMethodCallWithoutErrorBranches() {

		EvoSuite evosuite = new EvoSuite();

		String targetClass = Methodcall.class.getCanonicalName();

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
	public void testMethodCallWithErrorBranches() {

		EvoSuite evosuite = new EvoSuite();

		String targetClass = Methodcall.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.ERROR_BRANCHES = true;
		// Null strings are not so likely, so we give more budget
		Properties.SEARCH_BUDGET = 20000;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		// 4: 2 regular branches, 2 for NPE
		Assert.assertEquals("Wrong number of goals: ", 4, goals);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}

	@Test
	public void testFieldWithoutErrorBranches() {

		EvoSuite evosuite = new EvoSuite();

		String targetClass = Fieldaccess.class.getCanonicalName();

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
	public void testFieldWithErrorBranches() {

		EvoSuite evosuite = new EvoSuite();

		String targetClass = Fieldaccess.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.ERROR_BRANCHES = true;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		// 4: 2 regular branches, 2 for NPE on callee, 2 for NPE
		Assert.assertEquals("Wrong number of goals: ", 6, goals);
		// One goal is infeasible - null on this
		Assert.assertEquals("Non-optimal coverage: ", 5d / 6d, best.getCoverage(), 0.001);
	}
}
