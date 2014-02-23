package org.evosuite.testcase;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.ga.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.staticfield.UnstableAssertion;

public class TestUnstableAssertion extends SystemTest{

	private boolean reset_statick_field__property;
	private boolean junit_check_property;
	private boolean junit_tests_property;
	
	@Before
	public void saveProperties() {
		reset_statick_field__property = Properties.RESET_STATIC_FIELDS;
		junit_check_property = Properties.JUNIT_CHECK;
		junit_tests_property = Properties.JUNIT_TESTS;

		Properties.RESET_STATIC_FIELDS = true;
		Properties.JUNIT_CHECK = true;
		Properties.JUNIT_TESTS = true;
	}

	@After
	public void restoreProperties() {
		Properties.RESET_STATIC_FIELDS = reset_statick_field__property ;
		Properties.JUNIT_CHECK = junit_check_property;
		Properties.JUNIT_TESTS = junit_tests_property;

	}

	@Test
	public void test() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = UnstableAssertion.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;
		String[] command = new String[] {"-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);
		double best_fitness = best.getFitness();
		Assert.assertTrue("Optimal coverage was not achieved ", best_fitness == 0.0);
		
	}

}
