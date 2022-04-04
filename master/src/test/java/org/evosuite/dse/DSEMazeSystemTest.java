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
package org.evosuite.dse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assume.assumeTrue;

import com.examples.with.different.packagename.solver.MazeClientArrays;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.Properties.SolverType;
import org.evosuite.Properties.StoppingCondition;
import org.evosuite.Properties.Strategy;
import org.evosuite.SystemTestBase;
import org.evosuite.symbolic.dse.algorithm.ExplorationAlgorithmBase;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.solver.MazeClient;

public class DSEMazeSystemTest extends SystemTestBase {

    @Before
    public void init() {
        Properties.VIRTUAL_FS = true;
        Properties.VIRTUAL_NET = true;
        Properties.SEARCH_BUDGET = 50000;
        // Properties.CONCOLIC_TIMEOUT = Integer.MAX_VALUE;
        Properties.RESET_STATIC_FIELD_GETS = true;

        String cvc4_path = System.getenv("CVC4_PATH");
        if (cvc4_path != null) {
            Properties.CVC4_PATH = cvc4_path;
        }

        Properties.DSE_SOLVER = SolverType.CVC4_SOLVER;

        Properties.STOPPING_CONDITION = StoppingCondition.MAXTESTS;
        Properties.SEARCH_BUDGET = 300; // tests
        Properties.MINIMIZATION_TIMEOUT = 60 * 60;
        Properties.ASSERTION_TIMEOUT = 60 * 60;

        Properties.STRATEGY = Strategy.DSE;

        Properties.CRITERION = new Criterion[]{Criterion.BRANCH};

        Properties.MINIMIZE = true;
        Properties.ASSERTIONS = true;

        assumeTrue(Properties.CVC4_PATH != null);
    }

    @Test
    public void testMazeClientInput() {

        EvoSuite evosuite = new EvoSuite();
        String targetClass = MazeClient.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        String[] command = new String[]{"-generateSuiteUsingDSE", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        ExplorationAlgorithmBase dse = getDSEAFromResult(result);
        TestSuiteChromosome best = dse.getGeneratedTestSuite();

        System.out.println("EvolvedTestSuite:\n" + best);

        assertFalse(best.getTests().isEmpty());

        assertEquals(27, best.getNumOfCoveredGoals());
        assertEquals(0, best.getNumOfNotCoveredGoals());
    }

    @Test
    public void testMazeClientInputForArraysSupportUsingLazyVariables() {

        EvoSuite evosuite = new EvoSuite();
        String targetClass = MazeClientArrays.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        Properties.SELECTED_DSE_ARRAYS_MEMORY_MODEL_VERSION = Properties.DSE_ARRAYS_MEMORY_MODEL_VERSION.LAZY_VARIABLES;

        String[] command = new String[]{"-generateSuiteUsingDSE", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        ExplorationAlgorithmBase dse = getDSEAFromResult(result);
        TestSuiteChromosome best = dse.getGeneratedTestSuite();

        System.out.println("EvolvedTestSuite:\n" + best);

        assertFalse(best.getTests().isEmpty());

        assertEquals(26, best.getNumOfCoveredGoals());
        assertEquals(0, best.getNumOfNotCoveredGoals());
    }

    @Test
    public void testMazeClientInputForArraysSupportUsingArrayExpressions() {

        EvoSuite evosuite = new EvoSuite();
        String targetClass = MazeClientArrays.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;
        Properties.SELECTED_DSE_ARRAYS_MEMORY_MODEL_VERSION = Properties.DSE_ARRAYS_MEMORY_MODEL_VERSION.SELECT_STORE_EXPRESSIONS;

        String[] command = new String[]{"-generateSuiteUsingDSE", "-class", targetClass};

        Object result = evosuite.parseCommandLine(command);
        ExplorationAlgorithmBase dse = getDSEAFromResult(result);
        TestSuiteChromosome best = dse.getGeneratedTestSuite();

        System.out.println("EvolvedTestSuite:\n" + best);

        assertFalse(best.getTests().isEmpty());

        assertEquals(26, best.getNumOfCoveredGoals());
        assertEquals(0, best.getNumOfNotCoveredGoals());
    }


}
