package org.evosuite.utils;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.TestSuiteGenerator;
import org.evosuite.ga.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.ReadFromSystemIn;

public class SystemInUtilSystemTest extends SystemTest{

	private static final boolean defaultSystemIn = Properties.REPLACE_SYSTEM_IN;
	
	@After
	public void tearDown(){
		Properties.REPLACE_SYSTEM_IN = defaultSystemIn;
	}
	
	@Test
	public void test(){

		EvoSuite evosuite = new EvoSuite();

		String targetClass = ReadFromSystemIn.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.REPLACE_SYSTEM_IN= true;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();		
		Assert.assertEquals(3, goals );
		double coverage = best.getCoverage();
		Assert.assertTrue("Not good enough coverage: "+coverage, coverage > 0.99d);

	}
}
