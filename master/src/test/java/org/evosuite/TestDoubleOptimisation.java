package org.evosuite;

import org.evosuite.ga.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.DoubleExample;
import com.examples.with.different.packagename.DoubleExample2;

public class TestDoubleOptimisation extends SystemTest {
	
	private double seedConstants = Properties.PRIMITIVE_POOL;
	
	@After
	public void resetSeedConstants() {
		Properties.PRIMITIVE_POOL = seedConstants;
	}
	
	@Test
	public void testDoubleSUT() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = DoubleExample.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.PRIMITIVE_POOL = 0.0;
		Properties.SEARCH_BUDGET = 30000;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		Assert.assertTrue(result != null);

		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
	
	@Test
	public void testDoubleSUTExact() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = DoubleExample2.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		//Properties.PRIMITIVE_POOL = 0.0;
		// TODO: Optimising exact doubles without seeding takes _long_
		//Properties.SEARCH_BUDGET = 30000;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		Assert.assertTrue(result != null);

		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
}
