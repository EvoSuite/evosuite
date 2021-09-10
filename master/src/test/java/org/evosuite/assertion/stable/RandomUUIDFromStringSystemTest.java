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
package org.evosuite.assertion.stable;

import static org.junit.Assert.assertEquals;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.Properties.Criterion;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.stable.RandomUUIDFromStringUser;

public class RandomUUIDFromStringSystemTest extends SystemTestBase {
    private final boolean DEFAULT_REPLACE_CALLS = Properties.REPLACE_CALLS;
    private final Properties.JUnitCheckValues DEFAULT_JUNIT_CHECK = Properties.JUNIT_CHECK;
    private final boolean DEFAULT_JUNIT_TESTS = Properties.JUNIT_TESTS;
    private final boolean DEFAULT_PURE_INSPECTORS = Properties.PURE_INSPECTORS;
    private final boolean DEFAULT_SANDBOX = Properties.SANDBOX;

    @Before
    public void configureProperties() {
        Properties.SANDBOX = true;
        Properties.REPLACE_CALLS = true;
        Properties.JUNIT_CHECK = Properties.JUnitCheckValues.TRUE;
        Properties.JUNIT_TESTS = true;
        Properties.PURE_INSPECTORS = true;

    }

    @After
    public void restoreProperties() {
        Properties.SANDBOX = DEFAULT_SANDBOX;
        Properties.REPLACE_CALLS = DEFAULT_REPLACE_CALLS;
        Properties.JUNIT_CHECK = DEFAULT_JUNIT_CHECK;
        Properties.JUNIT_TESTS = DEFAULT_JUNIT_TESTS;
        Properties.PURE_INSPECTORS = DEFAULT_PURE_INSPECTORS;
    }

    @Test
    public void testRandomUUIDFromString() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = RandomUUIDFromStringUser.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        Properties.CRITERION = new Properties.Criterion[]{Criterion.BRANCH};

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        assertEquals("Sub optiomal coverage", 1.0d, best.getCoverage(), 0.00001);
    }

}
