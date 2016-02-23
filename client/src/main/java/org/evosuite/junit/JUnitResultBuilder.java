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
