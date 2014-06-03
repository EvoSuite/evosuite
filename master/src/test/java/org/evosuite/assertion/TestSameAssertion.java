package org.evosuite.assertion;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.Properties.AssertionStrategy;
import org.evosuite.SystemTest;
import org.evosuite.ga.GeneticAlgorithm;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.assertion.ArrayObjects;
import com.examples.with.different.packagename.assertion.ArrayPrimitiveWrapper;
import com.examples.with.different.packagename.assertion.WrapperCreatingCopy;
import com.examples.with.different.packagename.assertion.WrapperExample;

public class TestSameAssertion extends SystemTest {

	private Properties.AssertionStrategy strategy = null;
	
	@Before
	public void storeAssertionStrategy() {
		strategy = Properties.ASSERTION_STRATEGY;
	}
	
	@After
	public void restoreAssertionStrategy() {
		Properties.ASSERTION_STRATEGY = strategy;
	}
	
	/*
	 * SameAssertions on primitive/wrapper arrays are problematic,
	 * so we do not want to have them at all.
	 */
	@Test
	public void testPrimitiveArray() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = ArrayPrimitiveWrapper.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.ASSERTION_STRATEGY = AssertionStrategy.ALL;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);

		boolean hasSameAssertion = false;
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		for(TestChromosome testChromosome : best.getTestChromosomes()) {
			for(Assertion assertion : testChromosome.getTestCase().getAssertions()) {
				if(assertion instanceof SameAssertion) {
					hasSameAssertion = true;
					Assert.assertEquals(true, ((SameAssertion)assertion).value);
				}
			}
		}
		System.out.println("EvolvedTestSuite:\n" + best);
		Assert.assertFalse(hasSameAssertion);
	}
	
	@Test
	public void testObjectArray() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = ArrayObjects.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.SEARCH_BUDGET = 100000;
		Properties.ASSERTION_STRATEGY = AssertionStrategy.ALL;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);

		boolean hasSameAssertion = false;
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		for(TestChromosome testChromosome : best.getTestChromosomes()) {
			for(Assertion assertion : testChromosome.getTestCase().getAssertions()) {
				if(assertion instanceof SameAssertion) {
					hasSameAssertion = true;
					Assert.assertEquals(false, ((SameAssertion)assertion).value);
				}
			}
		}
		System.out.println("EvolvedTestSuite:\n" + best);
		Assert.assertTrue(hasSameAssertion);

	}
	
	@Test
	public void testWrapper() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = WrapperExample.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.ASSERTION_STRATEGY = AssertionStrategy.ALL;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);
		boolean hasSameAssertion = false;
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		for(TestChromosome testChromosome : best.getTestChromosomes()) {
			for(Assertion assertion : testChromosome.getTestCase().getAssertions()) {
				if(assertion instanceof SameAssertion) {
					hasSameAssertion = true;
					Assert.assertEquals(true, ((SameAssertion)assertion).value);
				}
			}
		}
		System.out.println("EvolvedTestSuite:\n" + best);
		Assert.assertTrue(hasSameAssertion);

	}
	
	@Test
	public void testWrapperCopy() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = WrapperCreatingCopy.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.ASSERTION_STRATEGY = AssertionStrategy.ALL;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);

		boolean hasSameAssertion = false;
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		for(TestChromosome testChromosome : best.getTestChromosomes()) {
			for(Assertion assertion : testChromosome.getTestCase().getAssertions()) {
				if(assertion instanceof SameAssertion) {
					hasSameAssertion = true;
					Assert.assertEquals(false, ((SameAssertion)assertion).value);
				}
			}
		}
		System.out.println("EvolvedTestSuite:\n" + best);
		Assert.assertTrue(hasSameAssertion);

	}
}
