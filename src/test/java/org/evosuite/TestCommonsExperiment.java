package org.evosuite;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.ga.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Test;

public class TestCommonsExperiment extends SystemTest {

	@Test
	public void testPredicatedMap() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = "org.apache.commons.collections.map.PredicatedMap";

		Properties.TARGET_CLASS = targetClass;
		
		
		String[] command = new String[] { "-generateSuite", "-class", targetClass, "-projectCP", "/Users/jmr/Dropbox/Papers/evosuite-study/experiment/object_selection/eclipse_projects/commons-predicatedmap/target/classes"};

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		//Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
	
	@Test
	public void testListPopulation() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = "org.apache.commons.math3.genetics.ListPopulation";

		Properties.TARGET_CLASS = targetClass;
		
		
		String[] command = new String[] { "-generateSuite", "-class", targetClass, "-projectCP", "/Users/jmr/Dropbox/Papers/evosuite-study/experiment/object_selection/eclipse_projects/commons-listpopulation/target/classes"};

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		//Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}

	/**
	 * Reveals odd behaviour in TestCluster.java. Method TestCluster.getGeneratorsForSpecialCase
	 * fails to create collection of generators for special case class
	 * org.apache.commons.collections.bidimap.UnmodifiableBidiMap
	 */
	@Test
	public void testRandomGeneratorForMaps() {
		
		EvoSuite evosuite = new EvoSuite();

		String targetClass = "org.apache.commons.collections.bidimap.UnmodifiableBidiMap";

		Properties.TARGET_CLASS = targetClass;
		
		
		String[] command = new String[] { "-generateSuite", "-class", targetClass, "-projectCP", 
				"/Users/jmr/Systems/commons/commons-collections-3.2.1-src/target/classes/","-Dlog.level=debug"};

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		//Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
}