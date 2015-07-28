/**
 * 
 */
package org.evosuite.junit;

import org.evosuite.testcase.execution.ExecutionTracer;
import org.evosuite.utils.LoggingUtils;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/**
 * <p>
 * JUnitRunListener class
 * </p>
 * 
 * @author José Campos
 */
public class JUnitRunListener extends RunListener {

	/**
	 * 
	 */
	private JUnitRunner junitRunner = null;

	/**
	 * 
	 */
	private JUnitResult testResult = null;

	/**
	 * 
	 */
	private long start;

	/**
	 * 
	 * @param jr
	 */
	public JUnitRunListener(JUnitRunner jR) {
		this.junitRunner = jR;
	}

	/**
	 * Called before any tests have been run
	 */
	@Override
	public void testRunStarted(Description description) {
		LoggingUtils.getEvoLogger().info("* Number of test cases to execute: " + description.testCount());
	}

	/**
	 * Called when all tests have finished
	 */
	@Override
	public void testRunFinished(Result result) {
		LoggingUtils.getEvoLogger().info("* Number of test cases to executed: " + result.getRunCount());
	}

	/**
	 * Called when an atomic test is about to be started
	 */
	@Override
	public void testStarted(Description description) {
		LoggingUtils.getEvoLogger().info("* Started: " + "ClassName: " + description.getClassName() + ", MethodName: " + description.getMethodName());

		this.start = System.nanoTime();

		this.testResult = new JUnitResult(description.getClassName() + "#" + description.getMethodName(), this.junitRunner.getJUnitClass());
	}

	/**
	 * Called when an atomic test has finished. whether the test successes or fails
	 */
	@Override
	public void testFinished(Description description) {
		LoggingUtils.getEvoLogger().info("* Finished: " + "ClassName: " + description.getClassName() + ", MethodName: " + description.getMethodName());

		this.testResult.setRuntime(System.nanoTime() - this.start);
		this.testResult.setExecutionTrace(ExecutionTracer.getExecutionTracer().getTrace());
		this.testResult.incrementRunCount();
		ExecutionTracer.getExecutionTracer().clear();

		this.junitRunner.addResult(this.testResult);
	}

	/**
	 * Called when an atomic test fails
	 */
	@Override
	public void testFailure(Failure failure) {
		LoggingUtils.getEvoLogger().info("* Failure: " + failure.getMessage());
		for (StackTraceElement s : failure.getException().getStackTrace()) {
			LoggingUtils.getEvoLogger().info("   " + s.toString());
		}

		this.testResult.setSuccessful(false);
		this.testResult.setTrace(failure.getTrace());
		this.testResult.incrementFailureCount();
	}

	/**
	 * Called when a test will not be run, generally because a test method is annotated with Ignore
	 */
	@Override
	public void testIgnored(Description description) throws java.lang.Exception {
		LoggingUtils.getEvoLogger().info("* Ignored: " + "ClassName: " + description.getClassName() + ", MethodName: " + description.getMethodName());
	}
}
