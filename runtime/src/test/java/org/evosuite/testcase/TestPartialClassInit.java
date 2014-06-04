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

import com.examples.with.different.packagename.staticfield.PartialClassInit;

public class TestPartialClassInit extends SystemTest{

	private boolean reset_statick_field__property;
	
	@Before
	public void saveProperties() {
		reset_statick_field__property = Properties.RESET_STATIC_FIELDS;
		Properties.RESET_STATIC_FIELDS = true;
	}

	@After
	public void restoreProperties() {
		Properties.RESET_STATIC_FIELDS = reset_statick_field__property ;
	}

	@Test
	public void test() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = PartialClassInit.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;
		String[] command = new String[] {"-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);
		double best_fitness = best.getFitness();
		Assert.assertTrue("Optimal coverage was not achieved ", best_fitness == 0.0);
		
	}

}
