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
	
	/*
	 * We consider VFS has it might mess up System.in handling (eg, these test cases were
	 * created to debug such issue that actually happened) 
	 */
	private static final boolean defaultVFS = Properties.VIRTUAL_FS;
	
	@After
	public void tearDown(){
		Properties.REPLACE_SYSTEM_IN = defaultSystemIn;
		Properties.VIRTUAL_FS = defaultVFS;
	}
	
	@Test
	public void testWithNoVFS(){
		Properties.VIRTUAL_FS = false;
		_test();
	}
	
	@Test
	public void testWithVFS(){
		Properties.VIRTUAL_FS = true;
		_test();
	}
	
	private void _test(){

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
