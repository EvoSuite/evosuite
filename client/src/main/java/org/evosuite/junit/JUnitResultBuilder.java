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

import org.apache.commons.lang3.tuple.Pair;
import org.junit.internal.Throwables;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Creates a JUnitResult instance from
 * a org.junit.runner.Result object.
 *
 * @author galeotti
 */
public class JUnitResultBuilder {

    private static final Logger logger = LoggerFactory.getLogger(JUnitResultBuilder.class);

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

    public JUnitResult build(List<Pair<TestIdentifier, TestExecutionResult>> results) {
        boolean wasSuccessful = results.stream().map(Pair::getRight).noneMatch(r -> r.getStatus() != TestExecutionResult.Status.SUCCESSFUL);
        int failureCount = (int) results.stream().map(Pair::getRight).filter(r -> r.getStatus() != TestExecutionResult.Status.SUCCESSFUL).count();
        int runCount = results.size();

        JUnitResult jUnitResult = new JUnitResult(wasSuccessful, failureCount, runCount);

        List<Pair<TestIdentifier, TestExecutionResult>> failures =
                results.stream().filter(r -> r.getRight().getStatus() == TestExecutionResult.Status.FAILED).collect(Collectors.toList());
        failures.stream().map(f -> toFailure(f.getLeft(), f.getRight())).forEach(jUnitResult::addFailure);
        return jUnitResult;
    }

    private static JUnitFailure toFailure(TestIdentifier identifier, TestExecutionResult failure) {
        String descriptionMethodName = identifier.getDisplayName();
        Throwable throwable = failure.getThrowable().get();
        String exceptionClassName = throwable.getClass().getName();
        String message = throwable.getMessage();
        String trace = Throwables.getStacktrace(throwable);
        boolean isAssertionError = (throwable instanceof java.lang.AssertionError);

        JUnitFailure jUnitFailure = new JUnitFailure(message, exceptionClassName, descriptionMethodName, isAssertionError, trace);
        for (StackTraceElement elem : throwable.getStackTrace()) {
            String elemToString = elem.toString();
            jUnitFailure.addToExceptionStackTrace(elemToString);
        }
        return jUnitFailure;
    }
}
