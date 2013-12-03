package org.evosuite;


import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.TestSuiteGenerator;
import org.evosuite.ga.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

public class TestClassInDefaultPackage extends SystemTest {
	@Test
	public void testClassInDefaultPackage() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = "ClassInDefaultPackage";

		Properties.TARGET_CLASS = targetClass;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		Assert.assertTrue(result != null);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		Assert.assertEquals("Wrong number of goals: ", 3, goals);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}

}
