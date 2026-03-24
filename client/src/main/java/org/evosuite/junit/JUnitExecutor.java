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
import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.runtime.classhandling.JDKClassResetter;
import org.evosuite.runtime.sandbox.Sandbox;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns;

final class JUnitExecutor {

    private static final Logger logger = LoggerFactory.getLogger(JUnitExecutor.class);

    private JUnitExecutor() {
    }

    static JUnitResult runJUnit(Class<?>[] testClasses) {
        if (Properties.TEST_FORMAT == Properties.OutputFormat.JUNIT4) {
            return runJUnit4(testClasses);
        }
        if (Properties.TEST_FORMAT == Properties.OutputFormat.JUNIT5) {
            return runJUnit5(testClasses);
        }
        throw new IllegalStateException("Can't run junit test with test format: " + Properties.TEST_FORMAT);
    }

    private static JUnitResult runJUnit4(Class<?>[] testClasses) {
        JUnitCore runner = new JUnitCore();
        boolean wasSandboxOn = Sandbox.isSandboxInitialized();
        Set<Thread> privileged = null;
        if (wasSandboxOn) {
            privileged = Sandbox.resetDefaultSecurityManager();
        }

        Result result;
        ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();
        try {
            TestGenerationContext.getInstance().goingToExecuteSUTCode();
            Thread.currentThread().setContextClassLoader(testClasses[0].getClassLoader());
            JDKClassResetter.reset();
            result = runner.run(testClasses);
        } finally {
            Thread.currentThread().setContextClassLoader(currentLoader);
            TestGenerationContext.getInstance().doneWithExecutingSUTCode();
            restoreSandboxState(wasSandboxOn, privileged);
        }

        return new JUnitResultBuilder().build(result);
    }

    private static JUnitResult runJUnit5(Class<?>[] testClasses) {
        boolean wasSandboxOn = Sandbox.isSandboxInitialized();
        Set<Thread> privileged = null;
        if (wasSandboxOn) {
            privileged = Sandbox.resetDefaultSecurityManager();
        }

        List<Pair<TestIdentifier, TestExecutionResult>> result = new ArrayList<>();
        ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();
        try {
            TestGenerationContext.getInstance().goingToExecuteSUTCode();
            Thread.currentThread().setContextClassLoader(testClasses[0].getClassLoader());
            JDKClassResetter.reset();
            LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                    .selectors(Arrays.stream(testClasses).map(DiscoverySelectors::selectClass).collect(Collectors.toList()))
                    .filters(includeClassNamePatterns(".*Test"))
                    .build();
            Launcher launcher = LauncherFactory.create();
            TestPlan testPlan = launcher.discover(request);
            launcher.registerTestExecutionListeners(new TestExecutionListener() {
                @Override
                public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
                    result.add(Pair.of(testIdentifier, testExecutionResult));
                }
            });
            launcher.execute(request);
        } finally {
            Thread.currentThread().setContextClassLoader(currentLoader);
            TestGenerationContext.getInstance().doneWithExecutingSUTCode();
            restoreSandboxState(wasSandboxOn, privileged);
        }

        return new JUnitResultBuilder().build(result);
    }

    private static void restoreSandboxState(boolean wasSandboxOn, Set<Thread> privileged) {
        if (wasSandboxOn) {
            if (!Sandbox.isSandboxInitialized()) {
                Sandbox.initializeSecurityManagerForSUT(privileged);
            }
            return;
        }

        if (Sandbox.isSandboxInitialized()) {
            logger.warn("EvoSuite problem: tests changed sandbox state, but they did not restore it after execution");
            Sandbox.resetDefaultSecurityManager();
        }
    }
}
