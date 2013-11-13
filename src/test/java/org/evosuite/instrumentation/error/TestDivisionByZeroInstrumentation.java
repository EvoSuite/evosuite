package org.evosuite.instrumentation.error;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.TestSuiteGenerator;
import org.evosuite.ga.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.errorbranch.DoubleDivisionByZero;
import com.examples.with.different.packagename.errorbranch.FloatDivisionByZero;
import com.examples.with.different.packagename.errorbranch.IntDivisionByZero;
import com.examples.with.different.packagename.errorbranch.LongDivisionByZero;

public class TestDivisionByZeroInstrumentation extends SystemTest {
	
	@Test
	public void testIntDivisionWithoutErrorBranches() {
		
		EvoSuite evosuite = new EvoSuite();

		String targetClass = IntDivisionByZero.class.getCanonicalName();

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
	public void testIntDivisionWithErrorBranches() {
		
		EvoSuite evosuite = new EvoSuite();

		String targetClass = IntDivisionByZero.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.ERROR_BRANCHES = true;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);

		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		// 6: 2 regular branches, 2 for overflow, 2 for division by zero
		// one of the overflow branches is infeasible
		Assert.assertTrue("Wrong number of goals: " + goals, goals > 4);
		Assert.assertEquals("Non-optimal coverage: ", 5d/6d, best.getCoverage(), 0.001);
	}
	
	@Test
	public void testFloatDivisionWithErrorBranches() {
		
		EvoSuite evosuite = new EvoSuite();

		String targetClass = FloatDivisionByZero.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.ERROR_BRANCHES = true;

		String[] command = new String[] { "-generateSuite", "-class", targetClass  };

		Object result = evosuite.parseCommandLine(command);

				GeneticAlgorithm<?> ga = getGAFromResult(result);

		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		// 6: 
		Assert.assertTrue("Wrong number of goals: " + goals, goals > 4);
		Assert.assertEquals("Non-optimal coverage: ", 5d/6d, best.getCoverage(), 0.001);
	}
	
	@Test
	public void testDoubleDivisionWithErrorBranches() {
		
		EvoSuite evosuite = new EvoSuite();

		String targetClass = DoubleDivisionByZero.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.ERROR_BRANCHES = true;

		String[] command = new String[] { "-generateSuite", "-class", targetClass  };

		Object result = evosuite.parseCommandLine(command);

				GeneticAlgorithm<?> ga = getGAFromResult(result);

		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		// 6: 
		Assert.assertTrue("Wrong number of goals: " + goals, goals > 4);
		Assert.assertEquals("Non-optimal coverage: ", 5d/6d, best.getCoverage(), 0.001);
	}
	
	@Test
	public void testLongDivisionWithErrorBranches() {
		
		EvoSuite evosuite = new EvoSuite();

		String targetClass = LongDivisionByZero.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.ERROR_BRANCHES = true;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

				GeneticAlgorithm<?> ga = getGAFromResult(result);

		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

		int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		// 6: 
		Assert.assertTrue("Wrong number of goals: " + goals, goals > 4);
		Assert.assertEquals("Non-optimal coverage: ", 5d/6d, best.getCoverage(), 0.001);
	}
}
