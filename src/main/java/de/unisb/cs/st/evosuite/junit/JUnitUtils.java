/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package de.unisb.cs.st.evosuite.junit;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.ma.UserFeedback;
import de.unisb.cs.st.evosuite.ma.parser.TestParser;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.ExecutionTrace;
import de.unisb.cs.st.evosuite.testcase.ExecutionTracer;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.TestCaseExecutor;

@SuppressWarnings("synthetic-access")
public class JUnitUtils {

	private static class LoggingUserFeedback implements UserFeedback {
		private final String resultsPath;

		public LoggingUserFeedback(String resultsPath) {
			super();
			this.resultsPath = resultsPath;
		}

		@Override
		public File chooseTargetFile(String className) {
			return new File(resultsPath + File.pathSeparator + className);
		}

		@Override
		public void showParseException(String message) {
			logger.error("ParseException: {}", message);
		}

	}

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(JUnitUtils.class);

	public static TestCase readTestCase(String failingTest) {
		String[] classpath = Properties.CLASSPATH;
		String[] sources = Properties.SOURCEPATH;
		TestCase testCase = new JUnitTestReader(classpath, sources).readJUnitTestCase(failingTest);
		return testCase;
	}

	public static Set<TestCase> readTestCases(String failingTest) {
		try {
			File failingTestFile = new File(failingTest);
			UserFeedback userFeedback = new LoggingUserFeedback(failingTestFile.getAbsoluteFile().getParent());
			TestParser parser = new TestParser(userFeedback);
			return parser.parseFile(failingTest);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

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

	public static ExecutionResult runTest(TestCase testCase) {
		Properties.TIMEOUT = 60 * 3 * 100;
		logger.debug("Execution testCase with timeout {}: \n{}", Properties.TIMEOUT, testCase.toCode());
		return TestCaseExecutor.getInstance().execute(testCase);
	}

	private JUnitUtils() {
		// cannot instantiate class
	}
}
