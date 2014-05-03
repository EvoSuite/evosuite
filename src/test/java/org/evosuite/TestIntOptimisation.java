package org.evosuite;

import org.evosuite.ga.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.IntExample;

public class TestIntOptimisation extends SystemTest {

	private double seedConstants = Properties.PRIMITIVE_POOL;
	
	@After
	public void resetSeedConstants() {
		Properties.PRIMITIVE_POOL = seedConstants;
	}
	
	@Test
	public void testIntSUT() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = IntExample.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.PRIMITIVE_POOL = 0.0;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		Assert.assertTrue(result != null);

		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
}
