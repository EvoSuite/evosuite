package org.evosuite;

import org.evosuite.ga.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.RegexBranch;

public class TestRegexBranch extends SystemTest {
	
	public static final double defaultDynamicPool = Properties.DYNAMIC_POOL;

	@After
	public void resetProperties() {
		Properties.DYNAMIC_POOL = defaultDynamicPool;
	}

	
	@Test
	public void testRegexBranch() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = RegexBranch.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.DYNAMIC_POOL = 1d / 3d;

		String[] command = new String[] { "-generateSuite", "-class", targetClass }; // , "-Dprint_to_system=true"

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
}
