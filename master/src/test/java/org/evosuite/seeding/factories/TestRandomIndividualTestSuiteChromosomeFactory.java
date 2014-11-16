package org.evosuite.seeding.factories;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.seeding.factories.RandomIndividualTestSuiteChromosomeFactory;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.staticusage.Class1;

public class TestRandomIndividualTestSuiteChromosomeFactory extends SystemTest {

	ChromosomeSampleFactory defaultFactory = new ChromosomeSampleFactory();
	TestSuiteChromosome bestIndividual;
	GeneticAlgorithm<TestSuiteChromosome> ga;

	@Before
	public void setup() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = Class1.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;
		String[] command = new String[] { "-generateSuite", "-class",
				targetClass };

		Object result = evosuite.parseCommandLine(command);

		ga = (GeneticAlgorithm<TestSuiteChromosome>) getGAFromResult(result);
		bestIndividual = (TestSuiteChromosome) ga.getBestIndividual();
	}

	@Test
	public void testNotSeed() {
		Properties.SEED_PROBABILITY = 0;
		RandomIndividualTestSuiteChromosomeFactory bicf = new RandomIndividualTestSuiteChromosomeFactory(
				defaultFactory, ga);
		assertEquals(bicf.getChromosome(), ChromosomeSampleFactory.CHROMOSOME);
	}

	@Test
	public void testRandomSeed() {
		Properties.SEED_PROBABILITY = 1;
		RandomIndividualTestSuiteChromosomeFactory bicf = new RandomIndividualTestSuiteChromosomeFactory(
				defaultFactory, ga);
		bicf.getChromosome();
		boolean isFromPopulation = false;
		TestSuiteChromosome tsc = bicf.getChromosome();
		for (TestSuiteChromosome t : ga.getPopulation()) {
			if (tsc.equals(t)) {
				isFromPopulation = true;
			}
		}
		assertTrue(isFromPopulation);
	}

}
