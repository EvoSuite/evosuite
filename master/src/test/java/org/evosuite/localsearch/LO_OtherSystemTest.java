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
import org.evosuite.Properties.Criterion;
import org.evosuite.Properties.StoppingCondition;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.sette.LO_Other;

/**
 * Created by J Galeotti on Aug 05 2016.
 */
public class LO_OtherSystemTest extends SystemTestBase {

    @Test
    public void testBase() {

        Properties.LOCAL_SEARCH_RATE = -1; // disable LS/DSE

        String targetClass = LO_Other.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.CRITERION = new Criterion[]{Criterion.BRANCH};

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        EvoSuite evosuite = new EvoSuite();
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

    }

    @Before
    public void init() {
        Properties.MINIMIZE = false;
        Properties.ASSERTIONS = false;
        Properties.STOPPING_CONDITION = StoppingCondition.MAXTIME;
        Properties.SEARCH_BUDGET = 30;
        Properties.P_FUNCTIONAL_MOCKING = 0.0d;
        Properties.P_REFLECTION_ON_PRIVATE = 0.0d;
    }

    @Test
    public void testDSE() {

        Properties.LOCAL_SEARCH_PROBABILITY = 1.0;
        Properties.LOCAL_SEARCH_RATE = 8;
        Properties.LOCAL_SEARCH_BUDGET_TYPE = Properties.LocalSearchBudgetType.TESTS;
        Properties.LOCAL_SEARCH_BUDGET = 100;
        Properties.DSE_PROBABILITY = 1.0; // force DSE, not LS
        Properties.DSE_SOLVER = Properties.SolverType.EVOSUITE_SOLVER;
        Properties.CONCOLIC_TIMEOUT = Integer.MAX_VALUE; // no timeout

        String targetClass = LO_Other.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        Properties.CRITERION = new Criterion[]{Criterion.BRANCH};

        String[] command = new String[]{"-generateSuite", "-class", targetClass};

        EvoSuite evosuite = new EvoSuite();
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

    }

}
