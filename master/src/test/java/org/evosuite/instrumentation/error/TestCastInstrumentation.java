package org.evosuite.instrumentation.error;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.strategy.TestGenerationStrategy;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.errorbranch.ClassCast;

public class TestCastInstrumentation extends SystemTest {
	
	@Test
	public void testCastWithoutErrorBranches() {
		
		EvoSuite evosuite = new EvoSuite();

		String targetClass = ClassCast.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
		Assert.assertEquals("Wrong number of goals: ", 2, goals);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
	
	@Test
	public void testCastWithErrorBranches() {
		
		EvoSuite evosuite = new EvoSuite();

		String targetClass = ClassCast.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.ERROR_BRANCHES = true;

		String[] command = new String[] { "-generateSuite", "-class", targetClass  };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
		Assert.assertEquals("Wrong number of goals: ", 6, goals);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
}
