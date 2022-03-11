/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * <p>
 * This file is part of EvoSuite.
 * <p>
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 * <p>
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.junit;

import org.evosuite.testcase.execution.ExecutionTracer;
import org.evosuite.utils.LoggingUtils;
import org.junit.internal.Throwables;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JUnit5RunListener implements TestExecutionListener {
    private static final Logger logger = LoggerFactory.getLogger(JUnit5RunListener.class);
    private final JUnitRunner jUnitRunner;


    private JUnitResult testResult = null;


    private long start;

    public JUnit5RunListener(JUnitRunner jUnitRunner) {
        this.jUnitRunner = jUnitRunner;
    }

    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        LoggingUtils.getEvoLogger().info("* Number of test cases to execute: " + testPlan.countTestIdentifiers(ignored -> true));
    }

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        LoggingUtils.getEvoLogger().info("* Number of test cases executed: No information available");
    }

    @Override
    public void dynamicTestRegistered(TestIdentifier testIdentifier) {
    }

    @Override
    public void executionSkipped(TestIdentifier testIdentifier, String reason) {
        LoggingUtils.getEvoLogger().info("* Ignored: " + "ClassName: " + testIdentifier.getDisplayName());
    }

    @Override
    public void executionStarted(TestIdentifier testIdentifier) {

        LoggingUtils.getEvoLogger().info("* Started: " + "ClassName: " + testIdentifier.getDisplayName());

        this.start = System.nanoTime();

        this.testResult = new JUnitResult(testIdentifier.getDisplayName(), this.jUnitRunner.getJUnitClass());
    }

    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        LoggingUtils.getEvoLogger().info("* Finished: " + "ClassName: " + testIdentifier.getDisplayName());
        if (testExecutionResult.getStatus() == TestExecutionResult.Status.SUCCESSFUL) {

            this.testResult.setRuntime(System.nanoTime() - this.start);
            this.testResult.setExecutionTrace(ExecutionTracer.getExecutionTracer().getTrace());
            this.testResult.incrementRunCount();
            ExecutionTracer.getExecutionTracer().clear();

            this.jUnitRunner.addResult(this.testResult);
        } else if (testExecutionResult.getStatus() == TestExecutionResult.Status.FAILED) {

            Throwable throwable = testExecutionResult.getThrowable().get();
            for (StackTraceElement s : throwable.getStackTrace()) {
                LoggingUtils.getEvoLogger().info("   " + s.toString());
            }

            this.testResult.setSuccessful(false);
            this.testResult.setTrace(Throwables.getStacktrace(throwable));
            this.testResult.incrementFailureCount();
        }
    }

    @Override
    public void reportingEntryPublished(TestIdentifier testIdentifier, ReportEntry entry) {
    }
}
