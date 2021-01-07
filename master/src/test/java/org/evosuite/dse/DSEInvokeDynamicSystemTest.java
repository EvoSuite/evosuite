/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.dse;

import com.examples.with.different.packagename.dse.StreamAPIExample;
import com.examples.with.different.packagename.dse.StringConcatenationExample;
import com.examples.with.different.packagename.dse.TestClosureClass;
import com.examples.with.different.packagename.dse.invokedynamic.dsc.instrument.SingleMethodReference;
import com.examples.with.different.packagename.dse.LambdaExample;
import org.apache.commons.lang3.NotImplementedException;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.symbolic.dse.algorithm.ExplorationAlgorithmBase;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Tests for the invokedynamic usages on java (JDK9 for now)
 *
 * @author Ignacio Lebrero
 */
public class DSEInvokeDynamicSystemTest extends DSESystemTestBase {

    /** Lambdas (JDK 8) */

	@Test
	public void testLambda() {
		EvoSuite evosuite = new EvoSuite();
		String targetClass = LambdaExample.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;
		Properties.SHOW_PROGRESS = true;

		String[] command = new String[] { "-generateSuiteUsingDSE", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		ExplorationAlgorithmBase dse = getDSEAFromResult(result);
		TestSuiteChromosome generatedTestSuite = dse.getGeneratedTestSuite();
		System.out.println("Generated Test Suite:\n" + generatedTestSuite);

		assertFalse(generatedTestSuite.getTests().isEmpty());

		assertEquals(6, generatedTestSuite.getNumOfCoveredGoals());
		assertEquals(1, generatedTestSuite.getNumOfNotCoveredGoals()); // Constructor cannot be reached
	}

	@Test
	public void testClosure() {
		EvoSuite evosuite = new EvoSuite();
		String targetClass = TestClosureClass.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;
		Properties.SHOW_PROGRESS = true;

		String[] command = new String[] { "-generateSuiteUsingDSE", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		ExplorationAlgorithmBase dse = getDSEAFromResult(result);
		TestSuiteChromosome generatedTestSuite = dse.getGeneratedTestSuite();
		System.out.println("Generated Test Suite:\n" + generatedTestSuite);

		assertFalse(generatedTestSuite.getTests().isEmpty());

		assertEquals(8, generatedTestSuite.getNumOfCoveredGoals());
		assertEquals(1, generatedTestSuite.getNumOfNotCoveredGoals());
	}

	/** Method references (JDK 8) */

	@Test
	public void testMethodReference() {
		EvoSuite evosuite = new EvoSuite();
		String targetClass = SingleMethodReference.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;
		Properties.SHOW_PROGRESS = true;

		String[] command = new String[] { "-generateSuiteUsingDSE", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		ExplorationAlgorithmBase dse = getDSEAFromResult(result);
		TestSuiteChromosome generatedTestSuite = dse.getGeneratedTestSuite();
		System.out.println("Generated Test Suite:\n" + generatedTestSuite);

		assertFalse(generatedTestSuite.getTests().isEmpty());

		assertEquals(7, generatedTestSuite.getNumOfCoveredGoals());
		assertEquals(0, generatedTestSuite.getNumOfNotCoveredGoals());
	}

	/**
	 * We are not currently supporting the Stream API as it calls lambdas from a non-instrumented context.
	 */
	@Test
	public void testStreamAPI() {
		EvoSuite evosuite = new EvoSuite();
		String targetClass = StreamAPIExample.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;
		Properties.SHOW_PROGRESS = true;

		String[] command = new String[] { "-generateSuiteUsingDSE", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		ExplorationAlgorithmBase dse = getDSEAFromResult(result);
		TestSuiteChromosome generatedTestSuite = dse.getGeneratedTestSuite();
		System.out.println("Generated Test Suite:\n" + generatedTestSuite);

		assertFalse(generatedTestSuite.getTests().isEmpty());

		assertEquals(4, generatedTestSuite.getNumOfCoveredGoals());
		assertEquals(8, generatedTestSuite.getNumOfNotCoveredGoals());
	}

	@Test
	@Ignore
	public void testAutoBoxingConversions() {
		throw new NotImplementedException("Implement me!");
	}

	/** String concatenation (JDK 9) */

	@Test
	public void testStringConcatenation() {
		EvoSuite evosuite = new EvoSuite();
		String targetClass = StringConcatenationExample.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;
		Properties.SHOW_PROGRESS = true;

		String[] command = new String[] { "-generateSuiteUsingDSE", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		ExplorationAlgorithmBase dse = getDSEAFromResult(result);
		TestSuiteChromosome generatedTestSuite = dse.getGeneratedTestSuite();
		System.out.println("Generated Test Suite:\n" + generatedTestSuite);

		assertFalse(generatedTestSuite.getTests().isEmpty());

		assertEquals(2, generatedTestSuite.getNumOfCoveredGoals());
		assertEquals(1, generatedTestSuite.getNumOfNotCoveredGoals());
	}

	/** Method Handles (JDK 8) */
	// TODO: complete eventually, for now we won't support it as we don't support the reflection API either
}