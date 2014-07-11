package org.evosuite.seeding;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.Properties.TestFactory;
import org.evosuite.SystemTest;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.testcarver.ArrayConverterTestCase;
import com.examples.with.different.packagename.testcarver.DifficultClassTest;
import com.examples.with.different.packagename.testcarver.DifficultClassWithoutCarving;
import com.examples.with.different.packagename.testcarver.DifficultClassWithoutCarvingTest;

public class TestObjectPool extends SystemTest {

	private double P_POOL = Properties.P_OBJECT_POOL;
	private boolean CARVE_POOL = Properties.CARVE_OBJECT_POOL;
	private String SELECTED_JUNIT = Properties.SELECTED_JUNIT;
	private TestFactory FACTORY = Properties.TEST_FACTORY;
	
	@After
	public void restoreProperties() {
		Properties.P_OBJECT_POOL = P_POOL;
		Properties.CARVE_OBJECT_POOL = CARVE_POOL;
		Properties.SELECTED_JUNIT = SELECTED_JUNIT;
		Properties.TEST_FACTORY = FACTORY;
	}
	
	@Test
	public void testDifficultClassWithoutPoolFails() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = DifficultClassWithoutCarving.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;
		Properties.P_OBJECT_POOL = 0.0;
				
		String[] command = new String[] { "-generateSuite", "-class", targetClass};

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		Assert.assertTrue("Did not expect optimal coverage: ", best.getCoverage() < 1d);		
	}
	
	@Test
	public void testDifficultClassWithWrongPoolFails() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = DifficultClassWithoutCarving.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;
		Properties.P_OBJECT_POOL = 0.8;
		Properties.CARVE_OBJECT_POOL = true;
		Properties.SELECTED_JUNIT = ArrayConverterTestCase.class.getCanonicalName();
				
		String[] command = new String[] { "-generateSuite", "-class", targetClass};

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		Assert.assertTrue("Did not expect optimal coverage: ", best.getCoverage() < 1d);		
	}
	
	@Test
	public void testDifficultClassWithPoolPasses() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = DifficultClassWithoutCarving.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;
		Properties.P_OBJECT_POOL = 0.8;
		Properties.CARVE_OBJECT_POOL = true;
		Properties.SELECTED_JUNIT = DifficultClassTest.class.getCanonicalName();
		
		String[] command = new String[] { "-generateSuite", "-class", targetClass};

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		Assert.assertEquals("Expected optimal coverage: ", 1d, best.getCoverage(), 0.001);		
	}
	
	@Test
	public void testDifficultClassWithMultipleClassPoolPasses() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = DifficultClassWithoutCarving.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;
		Properties.P_OBJECT_POOL = 0.8;
		Properties.CARVE_OBJECT_POOL = true;
		Properties.SELECTED_JUNIT = DifficultClassWithoutCarvingTest.class.getCanonicalName();
		
		String[] command = new String[] { "-generateSuite", "-class", targetClass};

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		Assert.assertEquals("Expected optimal coverage: ", 1d, best.getCoverage(), 0.001);		
	}


}
