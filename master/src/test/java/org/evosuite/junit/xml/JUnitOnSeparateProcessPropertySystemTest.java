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
package org.evosuite.junit.xml;

import static org.junit.Assert.assertFalse;

import java.util.Map;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.assertion.CheapPurityAnalyzer;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.statistics.OutputVariable;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.statistics.backend.DebugStatisticsBackend;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Type;

import com.examples.with.different.packagename.junit.Foo;
//import org.evosuite.testsuite.SearchStatistics;

public class JUnitOnSeparateProcessPropertySystemTest extends SystemTestBase {
    private final boolean DEFAULT_JUNIT_CHECK_ON_SEPARATE_PROCESS = Properties.JUNIT_CHECK_ON_SEPARATE_PROCESS;
    private final Properties.JUnitCheckValues DEFAULT_JUNIT_CHECK = Properties.JUNIT_CHECK;
    private final boolean DEFAULT_JUNIT_TESTS = Properties.JUNIT_TESTS;

    @Before
    public void saveProperties() {
        Properties.JUNIT_CHECK_ON_SEPARATE_PROCESS = true;
        Properties.JUNIT_CHECK = Properties.JUnitCheckValues.TRUE;
        Properties.JUNIT_TESTS = true;
    }

    @After
    public void restoreProperties() {
        Properties.JUNIT_CHECK_ON_SEPARATE_PROCESS = DEFAULT_JUNIT_CHECK_ON_SEPARATE_PROCESS;
        Properties.JUNIT_CHECK = DEFAULT_JUNIT_CHECK;
        Properties.JUNIT_TESTS = DEFAULT_JUNIT_TESTS;
    }

    @Test
    public void test() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = Foo.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        Properties.OUTPUT_VARIABLES = "" + RuntimeVariable.HadUnstableTests;
        String[] command = new String[]{"-generateSuite", "-class",
                targetClass};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);
        double best_fitness = best.getFitness();
        Assert.assertEquals("Optimal coverage was not achieved ", 0.0, best_fitness, 0.0);

        CheapPurityAnalyzer purityAnalyzer = CheapPurityAnalyzer.getInstance();

        String descriptor = Type.getMethodDescriptor(Type.BOOLEAN_TYPE,
                Type.getType(Object.class));
        boolean equals = purityAnalyzer.isPure(targetClass, "equals",
                descriptor);
        assertFalse(equals);

        Map<String, OutputVariable<?>> map = DebugStatisticsBackend.getLatestWritten();
        Assert.assertNotNull(map);
        OutputVariable unstable = map.get(RuntimeVariable.HadUnstableTests.toString());
        Assert.assertNotNull(unstable);
        Assert.assertEquals(Boolean.FALSE, unstable.getValue());
    }

}
