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
package org.evosuite.testcase;

import com.examples.with.different.packagename.staticfield.StaticFieldUninitialized;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.statistics.OutputVariable;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.statistics.backend.DebugStatisticsBackend;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

/**
 * Created by gordon on 30/11/2016.
 */
public class StaticUninitializedFieldSystemTest extends SystemTestBase {


    @Before
    public void setUpProperties() {
        Properties.RESET_STATIC_FIELDS = true;
        Properties.RESET_STATIC_FIELD_GETS = true;
        Properties.SANDBOX = true;
        Properties.JUNIT_CHECK = Properties.JUnitCheckValues.TRUE;
        Properties.JUNIT_TESTS = true;
        Properties.PURE_INSPECTORS = true;
        Properties.OUTPUT_VARIABLES = "" + RuntimeVariable.HadUnstableTests;
    }

    @Test
    public void test() {
        EvoSuite evosuite = new EvoSuite();

        String targetClass = StaticFieldUninitialized.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);

        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println(best.toString());
        Map<String, OutputVariable<?>> map = DebugStatisticsBackend.getLatestWritten();
        Assert.assertNotNull(map);
        OutputVariable<?> unstable = map.get(RuntimeVariable.HadUnstableTests.toString());
        Assert.assertNotNull(unstable);
        Assert.assertEquals("Unexpected unstabled test cases were generated", Boolean.FALSE, unstable.getValue());

        double best_fitness = best.getFitness();
        Assert.assertEquals("Optimal coverage was not achieved ", 0.0, best_fitness, 0.0);


    }

}
