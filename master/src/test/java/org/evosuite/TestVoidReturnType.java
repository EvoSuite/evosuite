package org.evosuite;

import org.evosuite.ga.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.VoidExample;

public class TestVoidReturnType extends SystemTest {
	@Test
	public void testVoidExample() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = VoidExample.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);

		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		String code = best.toString();
		System.out.println("EvolvedTestSuite:\n" + best);

		Assert.assertFalse(code.contains("Void "));
		Assert.assertFalse(code.contains("void0"));
	}
}
