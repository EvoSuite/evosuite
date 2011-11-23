package de.unisb.cs.st.evosuite.junit;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.ExecutionTrace;
import de.unisb.cs.st.evosuite.testcase.ExecutionTracer;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.TestCaseExecutor;

public class JUnitUtils {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(JUnitUtils.class);

	public static TestCase readTestCase(String failingTest) {
		String[] classpath = Properties.CLASSPATH;
		String[] sources = Properties.SOURCEPATH;
		TestCase testCase = new JUnitTestReader(classpath, sources).readJUnitTestCase(failingTest);
		return testCase;
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
		Properties.TIMEOUT = 60 * 3;
		logger.debug("Execution testCase with timeout {}: \n{}", Properties.TIMEOUT, testCase.toCode());
		return TestCaseExecutor.getInstance().execute(testCase);
	}

	private JUnitUtils() {
		// cannot instantiate class
	}
}
