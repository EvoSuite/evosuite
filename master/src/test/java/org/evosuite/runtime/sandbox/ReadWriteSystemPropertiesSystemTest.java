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
package org.evosuite.runtime.sandbox;

import static org.junit.Assert.fail;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.junit.JUnitAnalyzer;
import org.evosuite.result.TestGenerationResult;
import org.evosuite.result.TestGenerationResultBuilder;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Test;

import com.examples.with.different.packagename.sandbox.ReadLineSeparator;
import com.examples.with.different.packagename.sandbox.ReadWriteSystemProperties;

public class ReadWriteSystemPropertiesSystemTest extends SystemTestBase {

	private static final String userDir = System
			.getProperty(ReadWriteSystemProperties.USER_DIR);
	private static final String aProperty = System
			.getProperty(ReadWriteSystemProperties.A_PROPERTY);

	private final boolean DEFAULT_REPLACE_CALLS = Properties.REPLACE_CALLS;

	@After
	public void reset() {
		Properties.REPLACE_CALLS = DEFAULT_REPLACE_CALLS;
	}

	@BeforeClass
	public static void checkStatus() {
		//such property shouldn't exist
		Assert.assertNull(aProperty);
	}

	@Test
	public void testReadLineSeparator() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = ReadLineSeparator.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.SANDBOX = true;
		Properties.REPLACE_CALLS = true;

		String[] command = new String[] { "-generateSuite", "-class",
				targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);
		double cov = best.getCoverage();
		Assert.assertEquals("Non-optimal coverage: ", 1d, cov, 0.001);

		//now check the JUnit generation
		List<TestCase> list = best.getTests();
		int n = list.size();
		Assert.assertTrue(n > 0);

		TestCaseExecutor.initExecutor(); //needed because it gets pulled down after the search

		try {
			Sandbox.initializeSecurityManagerForSUT();
			JUnitAnalyzer.removeTestsThatDoNotCompile(list);
		} finally {
			Sandbox.resetDefaultSecurityManager();
		}
		Assert.assertEquals(n, list.size());

		TestGenerationResult tgr = TestGenerationResultBuilder.buildSuccessResult();
		String code = tgr.getTestSuiteCode();
		Assert.assertTrue("Test code:\n" + code, code.contains("line.separator"));
		
		/*
		 * This is tricky. The property 'debug' is read, but it does not exist. 
		 * Ideally, we should still have in the test case a call to be sure the variable
		 * is set to null. But that would lead to a lot of problems :( eg cases
		 * in which we end up in reading hundreds of thousands variables that do not exist
		 */
		Assert.assertTrue("Test code:\n" + code, ! code.contains("debug"));
	}

	@Test
	public void testNoReplace() {

		EvoSuite evosuite = new EvoSuite();

		String targetClass = ReadWriteSystemProperties.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.SANDBOX = true;
		Properties.REPLACE_CALLS = false;

		String[] command = new String[] { "-generateSuite", "-class",
				targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);
		double cov = best.getCoverage();
		//without replace calls, we shouldn't be able to achieve full coverage
		Assert.assertTrue(cov < 1d);
	}

	@Test
	public void testWithReplace() {

		EvoSuite evosuite = new EvoSuite();

		String targetClass = ReadWriteSystemProperties.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.SANDBOX = true;
		Properties.REPLACE_CALLS = true;

		String[] command = new String[] { "-generateSuite", "-class",
				targetClass };

		Object result = evosuite.parseCommandLine(command);

		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);
		double cov = best.getCoverage();
		Assert.assertEquals("Non-optimal coverage: ", 1d, cov, 0.001);

		//now check if properties have been reset to their initial state
		String currentUserDir = System
				.getProperty(ReadWriteSystemProperties.USER_DIR);
		String currentAProperty = System
				.getProperty(ReadWriteSystemProperties.A_PROPERTY);

		Assert.assertEquals(userDir, currentUserDir);
		Assert.assertEquals(aProperty, currentAProperty);

		//now check the JUnit generation
		List<TestCase> list = best.getTests();
		int n = list.size();
		Assert.assertTrue(n > 0);

		TestCaseExecutor.initExecutor(); //needed because it gets pulled down after the search

		try {
			Sandbox.initializeSecurityManagerForSUT();
			for (TestCase tc : list) {
				Assert.assertFalse(tc.isUnstable());
			}

			JUnitAnalyzer.removeTestsThatDoNotCompile(list);
			Assert.assertEquals(n, list.size());
			JUnitAnalyzer.handleTestsThatAreUnstable(list);
			Assert.assertEquals(n, list.size());

			for (TestCase tc : list) {
				Assert.assertFalse(tc.isUnstable());
			}

			Assert.assertEquals(userDir, currentUserDir);
			Assert.assertEquals(aProperty, currentAProperty);
		}  finally {
			Sandbox.resetDefaultSecurityManager();
		}
	}
}
