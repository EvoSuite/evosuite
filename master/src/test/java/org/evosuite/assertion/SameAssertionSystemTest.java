/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.assertion;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.Properties.AssertionStrategy;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.*;

import com.examples.with.different.packagename.assertion.ArrayObjects;
import com.examples.with.different.packagename.assertion.ArrayPrimitiveWrapper;
import com.examples.with.different.packagename.assertion.WrapperCreatingCopy;
import com.examples.with.different.packagename.assertion.WrapperExample;

public class SameAssertionSystemTest extends SystemTestBase {

	private Properties.AssertionStrategy strategy = null;
	
	private double nullProbability = Properties.NULL_PROBABILITY;
	
	private double primitiveReuseProbability = Properties.PRIMITIVE_REUSE_PROBABILITY;
	
	@Before
	public void storeAssertionStrategy() {
		strategy = Properties.ASSERTION_STRATEGY;
		nullProbability = Properties.NULL_PROBABILITY;
		primitiveReuseProbability = Properties.PRIMITIVE_REUSE_PROBABILITY;
	}
	
	@After
	public void restoreAssertionStrategy() {
		Properties.ASSERTION_STRATEGY = strategy;
		Properties.NULL_PROBABILITY = nullProbability;
		Properties.PRIMITIVE_REUSE_PROBABILITY = primitiveReuseProbability;
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
					//Assert.assertEquals(true, ((SameAssertion)assertion).value);
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

	// TODO: Same assertions are excluding wrapper classes for now, as there are issues
	//       when the values are inlined
	@Ignore
	@Test
	public void testWrapper() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = WrapperExample.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.ASSERTION_STRATEGY = AssertionStrategy.ALL;
		// If we allow null in this test, then there is a way
		// to cover the branch without assertions but with
		// exception
		Properties.NULL_PROBABILITY = 0.0;
		
		// Ensure that a new Integer object is created rather than 
		// just using an int, because there's no assertSame between
		// an int and an Integer
		Properties.PRIMITIVE_REUSE_PROBABILITY = 0.0;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);
		boolean hasSameAssertion = false;
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);
		for(TestChromosome testChromosome : best.getTestChromosomes()) {
			for(Assertion assertion : testChromosome.getTestCase().getAssertions()) {
				if(assertion instanceof SameAssertion) {
					hasSameAssertion = true;
					Assert.assertEquals(true, ((SameAssertion)assertion).value);
				}
			}
		}
		Assert.assertTrue(hasSameAssertion);

	}

	// TODO: Same assertions are excluding wrapper classes for now, as there are issues
	//       when the values are inlined
	@Ignore
	@Test
	public void testWrapperCopy() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = WrapperCreatingCopy.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.ASSERTION_STRATEGY = AssertionStrategy.ALL;
		// If we allow null in this test, then there is a way
		// to cover the branch without assertions but with
		// exception
		Properties.NULL_PROBABILITY = 0.0;
		
		// Ensure that a new Integer object is created rather than 
		// just using an int, because there's no assertSame between
		// an int and an Integer
		Properties.PRIMITIVE_REUSE_PROBABILITY = 0.0;

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
