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
package org.evosuite.localsearch;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.Properties.SolverType;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.localsearch.ExampleHardForGA;

/**
 * Created by Andrea Arcuri on 19/03/15.
 */
public class ExampleHardForGASystemTest extends SystemTestBase {

    @Before
    public void init() {
        Properties.RESET_STATIC_FIELD_GETS = true;
        Properties.P_FUNCTIONAL_MOCKING = 0.0;
        Properties.P_REFLECTION_ON_PRIVATE = 0.0;
        Properties.MINIMIZE = true;
        Properties.TEST_ARCHIVE = true;
    }

    @Test
    public void testZ3() {
        Assume.assumeTrue(System.getenv("z3_path") != null);
        Properties.Z3_PATH = System.getenv("z3_path");
        Properties.DSE_SOLVER = SolverType.Z3_SOLVER;

        Properties.LOCAL_SEARCH_PROBABILITY = 1.0;
        Properties.LOCAL_SEARCH_RATE = 8;
        Properties.LOCAL_SEARCH_BUDGET_TYPE = Properties.LocalSearchBudgetType.TESTS;
        Properties.LOCAL_SEARCH_BUDGET = 10;
        Properties.DSE_PROBABILITY = 1.0; // force using only DSE, no LS
        Properties.CONCOLIC_TIMEOUT = 1000;

        EvoSuite evosuite = new EvoSuite();
        String targetClass = ExampleHardForGA.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

    /**
     * It is expected that the regular GA will not be able to cover all the
     * branches in the given budget, but using a constraint solver will do it
     */
    @Test
    public void testBase() {

        Properties.LOCAL_SEARCH_RATE = -1; // disable LS

        EvoSuite evosuite = new EvoSuite();
        String targetClass = ExampleHardForGA.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        /**
         * We expect the coverage will not be 100% branch, since there is at
         * least one branch that is particularly hard for GA
         */
        Assert.assertTrue(best.getCoverage() < 1d);
    }

    @Test
    public void testCVC4() {
        Assume.assumeTrue(System.getenv("cvc4_path") != null);
        Properties.CVC4_PATH = System.getenv("cvc4_path");
        Properties.DSE_SOLVER = SolverType.CVC4_SOLVER;

        Properties.LOCAL_SEARCH_PROBABILITY = 1.0;
        Properties.LOCAL_SEARCH_RATE = 8;
        Properties.LOCAL_SEARCH_BUDGET_TYPE = Properties.LocalSearchBudgetType.TESTS;
        Properties.LOCAL_SEARCH_BUDGET = 10;
        Properties.DSE_PROBABILITY = 1.0; // force using only DSE, no LS
        Properties.CONCOLIC_TIMEOUT = 1000;

        EvoSuite evosuite = new EvoSuite();
        String targetClass = ExampleHardForGA.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

        Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
    }

}
