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
package org.evosuite.mock.java.io;

import java.util.Map;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.statistics.OutputVariable;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.statistics.backend.DebugStatisticsBackend;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.mock.java.util.RandomUser;
//import org.evosuite.testsuite.SearchStatistics;

public class MockRandomSystemTest extends SystemTestBase {

    private static final boolean REPLACE_CALLS = Properties.REPLACE_CALLS;
    private static final boolean JUNIT_TESTS = Properties.JUNIT_TESTS;
    private static final Properties.JUnitCheckValues JUNIT_CHECK = Properties.JUNIT_CHECK;

    @Before
    public void setProperties() {
        Properties.REPLACE_CALLS = true;
        Properties.JUNIT_TESTS = true;
        Properties.JUNIT_CHECK = Properties.JUnitCheckValues.TRUE;
    }

    @After
    public void restoreProperties() {
        Properties.REPLACE_CALLS = REPLACE_CALLS;
        Properties.JUNIT_TESTS = JUNIT_TESTS;
        Properties.JUNIT_CHECK = JUNIT_CHECK;
    }

    @Test
    public void testRandomUser() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = RandomUser.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.SEARCH_BUDGET = 20000;
        Properties.OUTPUT_VARIABLES = "" + RuntimeVariable.HadUnstableTests;

        String[] command = new String[]{"-generateSuite", "-class",
                targetClass};

        Object result = evosuite.parseCommandLine(command);
        Assert.assertNotNull(result);

        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(),
                0.001);

        Map<String, OutputVariable<?>> map = DebugStatisticsBackend.getLatestWritten();
        Assert.assertNotNull(map);
        OutputVariable<?> unstable = map.get(RuntimeVariable.HadUnstableTests.toString());
        Assert.assertNotNull(unstable);
        Assert.assertEquals(Boolean.FALSE, unstable.getValue());
    }

}
