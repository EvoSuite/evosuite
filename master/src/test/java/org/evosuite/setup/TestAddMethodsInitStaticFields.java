package org.evosuite.setup;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.ga.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.staticusage.Class1;
import com.examples.with.different.packagename.staticusage.DirectAccessStaticField;

public class TestAddMethodsInitStaticFields extends SystemTest {

	private boolean ADD_METHODS_INITIALIZING_STATIC_FIELDS;

	@Before
	public void prepareTest() {
		ADD_METHODS_INITIALIZING_STATIC_FIELDS = Properties.HANDLE_STATIC_FIELDS;
		Properties.HANDLE_STATIC_FIELDS = true;
	}

	@Test
	public void testStaticMethods() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = Class1.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;
		String[] command = new String[] { "-generateSuite", "-class",
				targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);
		double best_fitness = best.getFitness();
		Assert.assertTrue("Optimal coverage not reached: "+best_fitness, best_fitness == 0.0);
	}
	
	@Test
	public void testStaticFields() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = DirectAccessStaticField.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;
		String[] command = new String[] { "-generateSuite", "-class",
				targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);
		double best_fitness = best.getFitness();
		Assert.assertTrue("Optimal coverage not reached: "+best_fitness, best_fitness == 0.0);
	}
	
	@After
	public void restore() {
		Properties.HANDLE_STATIC_FIELDS = ADD_METHODS_INITIALIZING_STATIC_FIELDS;
	}


}
