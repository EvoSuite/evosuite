package org.evosuite.seeding.factories;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.seeding.factories.BestIndividualTestSuiteChromosomeFactory;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.staticusage.Class1;

public class TestBestIndividualTestSuiteChromosomeFactory extends SystemTest {
	ChromosomeSampleFactory defaultFactory = new ChromosomeSampleFactory();
	TestSuiteChromosome bestIndividual;
	GeneticAlgorithm<TestSuiteChromosome> ga;

	@Before
	public void setup(){
		setDefaultPropertiesForTestCases();

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
	public void testSeed(){
		BestIndividualTestSuiteChromosomeFactory bicf = new BestIndividualTestSuiteChromosomeFactory(
				defaultFactory, bestIndividual);
		
		assertEquals(bestIndividual.toString(), bicf.getChromosome().toString());
	}
	
	@Test
	public void testNotSeed(){
		BestIndividualTestSuiteChromosomeFactory bicf = new BestIndividualTestSuiteChromosomeFactory(
				defaultFactory, bestIndividual);
		bicf.getChromosome();
		assertFalse(bicf.getChromosome().equals(bestIndividual));
	}
}
