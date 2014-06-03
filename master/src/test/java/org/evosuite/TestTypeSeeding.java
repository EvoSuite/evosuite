package org.evosuite;

import org.evosuite.ga.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.TypeSeedingExampleGeneric;
import com.examples.with.different.packagename.TypeSeedingExampleLocale;
import com.examples.with.different.packagename.TypeSeedingExampleString;

public class TestTypeSeeding extends SystemTest {
	
	@Test
	public void testStringToObject() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = TypeSeedingExampleString.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.SEED_TYPES = true;


		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);

		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		Assert.assertEquals("Wrong number of goals: ", 5, goals);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
	
	@Test
	public void testStringToObjectNoSeeding() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = TypeSeedingExampleString.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.SEED_TYPES = false;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);

		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		Assert.assertEquals("Wrong number of goals: ", 5, goals);
		Assert.assertEquals("Non-optimal coverage: ", 2d/5d, best.getCoverage(), 0.001);
	}
	
	@Test
	public void testLocaleToObject() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = TypeSeedingExampleLocale.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.SEED_TYPES = true;


		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);

		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		Assert.assertEquals("Wrong number of goals: ", 3, goals);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
	
	@Test
	public void testLocaleToObjectNoSeeding() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = TypeSeedingExampleLocale.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.SEED_TYPES = false;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);

		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		Assert.assertEquals("Wrong number of goals: ", 3, goals);
		Assert.assertEquals("Non-optimal coverage: ", 2d/3d, best.getCoverage(), 0.001);
	}
	
	@Test
	public void testGenericObject() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = TypeSeedingExampleGeneric.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.SEED_TYPES = true;


		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);

		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		Assert.assertEquals("Wrong number of goals: ", 3, goals);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
	
	@Test
	public void testGenericObjectNoSeeding() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = TypeSeedingExampleGeneric.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.SEED_TYPES = false;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);

		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		Assert.assertEquals("Wrong number of goals: ", 3, goals);
		Assert.assertEquals("Non-optimal coverage: ", 2d/3d, best.getCoverage(), 0.001);
	}
}
