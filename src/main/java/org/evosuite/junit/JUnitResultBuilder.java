package org.evosuite.junit;

import java.util.List;

import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

/**
 * Creates a JUnitResult instance from 
 * a org.junit.runner.Result object.
 * 
 * @author galeotti
 *
 */
public class JUnitResultBuilder {

	/**
	 * Translates <i>part</i> of the org.junit.runner.Result object
	 * into an evosuite independent object.
	 * 
	 * @param result
	 * @return
	 */
	public JUnitResult build(Result result) {
		boolean wasSuccessful = result.wasSuccessful();
		int failureCount = result.getFailureCount();
		int runCount = result.getRunCount();

		JUnitResult junitResult = new JUnitResult(wasSuccessful, failureCount,
				runCount);
		
		List<Failure> failures = result.getFailures();

		for (Failure failure : failures) {
			String descriptionMethodName = failure.getDescription()
					.getMethodName();
			String exceptionClassName = failure.getException().getClass()
					.toString();
			String message = failure.getMessage();
			String trace = failure.getTrace();
			boolean isAssertionError = (failure.getException() instanceof java.lang.AssertionError);

			JUnitFailure junitFailure = new JUnitFailure(message,
					exceptionClassName, descriptionMethodName,
					isAssertionError, trace);

			for (StackTraceElement elem : failure.getException()
					.getStackTrace()) {
				String elemToString = elem.toString();
				junitFailure.addToExceptionStackTrace(elemToString);
			}
			junitResult.addFailure(junitFailure);
		}
		return junitResult;
	}
}
