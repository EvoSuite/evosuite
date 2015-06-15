package org.evosuite.seeding;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.strategy.TestGenerationStrategy;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.seeding.Range;
import com.examples.with.different.packagename.seeding.Range2;
import com.examples.with.different.packagename.seeding.RangeMin;


public class TestRange extends SystemTest {

	@Test
	public void testRangeTypeSeeding() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = Range.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.SEARCH_BUDGET = 10000;
		Properties.SEED_TYPES = true;


		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);

		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
		//Assert.assertEquals("Wrong number of goals: ", 22, goals);
		//Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
	@Test
	public void testRangeTypeSeedingOff() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = Range.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.SEARCH_BUDGET = 10000;
		Properties.SEED_TYPES = false;


		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);

		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
		//Assert.assertEquals("Wrong number of goals: ", 22, goals);
		//Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}

	@Test
	public void testRangeMinTypeSeeding() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = RangeMin.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.SEARCH_BUDGET = 10000;
		Properties.SEED_TYPES = true;


		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);

		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
		//Assert.assertEquals("Wrong number of goals: ", 22, goals);
		//Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
	@Test
	public void testRangeMinTypeSeedingOff() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = RangeMin.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.SEARCH_BUDGET = 10000;
		Properties.SEED_TYPES = false;


		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);

		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
		//Assert.assertEquals("Wrong number of goals: ", 22, goals);
		//Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}

	@Test
	public void testRange2TypeSeeding() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = Range2.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.SEARCH_BUDGET = 10000;
		Properties.SEED_TYPES = true;


		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);

		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		//int goals = TestSuiteGenerator.getFitnessFactory().get(0).getCoverageGoals().size(); // assuming single fitness function
		//Assert.assertEquals("Wrong number of goals: ", 22, goals);
		//Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
	@Test
	public void testRange2TypeSeedingOff() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = Range2.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.SEARCH_BUDGET = 10000;
		Properties.SEED_TYPES = false;


		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);

		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		//int goals = TestSuiteGenerator.getFitnessFactory().get(0).getCoverageGoals().size(); // assuming single fitness function
		//Assert.assertEquals("Wrong number of goals: ", 22, goals);
		Assert.assertTrue("Did not expect optimal coverage: ", best.getCoverage() < 1d);
	}
}