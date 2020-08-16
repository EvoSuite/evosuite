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
package org.evosuite.junit;

import java.util.HashSet;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.testcase.ExecutionResult;
import org.evosuite.testcase.ExecutionTrace;
import org.evosuite.testcase.ExecutionTracer;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestCaseExecutor;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.slf4j.Logger;

@SuppressWarnings("synthetic-access")
public class JUnitUtils {

	private static final Logger logger = org.slf4j.LoggerFactory.getLogger(JUnitUtils.class);

	/**
	 * <p>
	 * readTestCase
	 * </p>
	 * 
	 * @param failingTest
	 *            a {@link java.lang.String} object.
	 * @return a {@link org.evosuite.testcase.TestCase} object.
	 */
	public static TestCase readTestCase(String failingTest) {
		String[] classpath = Properties.CLASSPATH;
		String[] sources = Properties.SOURCEPATH;
		TestCase testCase = new JUnitTestReader(classpath, sources).readJUnitTestCase(failingTest);
		return testCase;
	}

	/**
	 * <p>
	 * readTestCases
	 * </p>
	 * 
	 * @param failingTest
	 *            a {@link java.lang.String} object.
	 * @return a {@link java.util.Set} object.
	 */
	public static Set<TestCase> readTestCases(String failingTest) {
		JUnitTestReader parser = new JUnitTestReader();
		Set<TestCase> tests = new HashSet<TestCase>();
		tests.addAll(parser.readTests(failingTest).values());
		return tests;
	}

	/**
	 * <p>
	 * runTest
	 * </p>
	 * 
	 * @param originalTest
	 *            a {@link java.lang.String} object.
	 * @return a {@link org.evosuite.junit.TestRun} object.
	 */
	public static TestRun runTest(String originalTest) {
		if (originalTest.contains("#")) {
			originalTest = originalTest.substring(0, originalTest.indexOf("#"));
		}
		try {
			ExecutionTracer.enable();
			Class<?> forName = null;
			forName = Class.forName(originalTest);
			logger.debug("Running against JUnit test suite " + originalTest);
			Result result = JUnitCore.runClasses(forName);
			assert result.getFailureCount() == 1 : "Cannot handle more or less than exactly one failure at a time.";
			Throwable failure = result.getFailures().get(0).getException();
			ExecutionTrace trace = ExecutionTracer.getExecutionTracer().getTrace();
			return new TestRun(trace, failure);
		} catch (ClassNotFoundException exc) {
			throw new RuntimeException(exc);
		}
	}

	/**
	 * <p>
	 * runTest
	 * </p>
	 * 
	 * @param testCase
	 *            a {@link org.evosuite.testcase.TestCase} object.
	 * @return a {@link org.evosuite.testcase.ExecutionResult} object.
	 */
	public static ExecutionResult runTest(TestCase testCase) {
		logger.debug("Execution testCase with timeout {}: \n{}", Properties.TIMEOUT,
		             testCase.toCode());
		return TestCaseExecutor.getInstance().execute(testCase);
	}

	private JUnitUtils() {
		// cannot instantiate class
	}
}
