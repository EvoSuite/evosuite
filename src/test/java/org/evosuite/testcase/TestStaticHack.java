package org.evosuite.testcase;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.ga.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.statichack.StaticHack;

public class TestStaticHack extends SystemTest{

	private boolean statick_hack_property;

	@Before
	public void saveProperties() {
		statick_hack_property = Properties.STATIC_HACK;
		Properties.STATIC_HACK = true;
	}

	@After
	public void restoreProperties() {
		Properties.STATIC_HACK = statick_hack_property ;
	}

	@Test
	public void test() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = StaticHack.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;
		String[] command = new String[] {"-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);
		double best_fitness = best.getFitness();
		Assert.assertTrue("Optimal coverage is not feasible ", best_fitness > 0.0);
		
	}

}
