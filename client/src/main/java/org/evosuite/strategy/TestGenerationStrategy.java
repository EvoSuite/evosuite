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
package org.evosuite.strategy;

import org.evosuite.ProgressMonitor;
import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.FitnessFunctionsUtils;
import org.evosuite.coverage.TestFitnessFactory;
import org.evosuite.ga.stoppingconditions.*;
import org.evosuite.graphs.cfg.CFGMethodAdapter;
import org.evosuite.instrumentation.InstrumentingClassLoader;
import org.evosuite.rmi.ClientServices;
import org.evosuite.setup.TestCluster;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;

import java.util.List;

/**
 * This is the abstract superclass of all techniques to generate a set of tests
 * for a target class, which does not necessarily require the use of a GA.
 * <p>
 * Postprocessing is not done as part of the test generation strategy.
 *
 * @author gordon
 */
public abstract class TestGenerationStrategy {

    /**
     * Generate a set of tests; assume that all analyses are already completed
     *
     * @return
     */
    public abstract TestSuiteChromosome generateTests();

    /**
     * There should only be one
     */
    protected final ProgressMonitor<TestSuiteChromosome> progressMonitor = new ProgressMonitor<>();

    /**
     * There should only be one
     */
    protected ZeroFitnessStoppingCondition<TestSuiteChromosome> zeroFitness =
            new ZeroFitnessStoppingCondition<>();

    /**
     * There should only be one
     */
    protected StoppingCondition<TestSuiteChromosome> globalTime =
            new GlobalTimeStoppingCondition<>();

    protected void sendExecutionStatistics() {
        ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Statements_Executed, MaxStatementsStoppingCondition.getNumExecutedStatements());
        ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Tests_Executed, MaxTestsStoppingCondition.getNumExecutedTests());
    }

    /**
     * Convert criterion names to test suite fitness functions
     *
     * @return
     */
    protected List<TestSuiteFitnessFunction> getFitnessFunctions() {
        return FitnessFunctionsUtils.getFitnessFunctions(Properties.CRITERION);
    }

    /**
     * Convert criterion names to factories for test case fitness functions
     *
     * @return
     */
    public static List<TestFitnessFactory<? extends TestFitnessFunction>> getFitnessFactories() {
        return FitnessFunctionsUtils.getFitnessFactories(Properties.CRITERION);
    }

    /**
     * Check if the budget has been used up. The GA will do this check
     * on its own, but other strategies (e.g. random) may depend on this function.
     *
     * @param chromosome
     * @param stoppingCondition
     * @return
     */
    protected boolean isFinished(TestSuiteChromosome chromosome,
                                 StoppingCondition<TestSuiteChromosome> stoppingCondition) {
        if (stoppingCondition.isFinished())
            return true;

        if (Properties.STOP_ZERO) {
            if (chromosome.getFitness() == 0.0)
                return true;
        }

        if (!(stoppingCondition instanceof MaxTimeStoppingCondition)) {
            return globalTime.isFinished();
        }

        return false;
    }

    /**
     * Convert property to actual stopping condition
     *
     * @return
     */
    protected StoppingCondition<TestSuiteChromosome> getStoppingCondition() {
        return StoppingConditionFactory.getStoppingCondition(Properties.STOPPING_CONDITION);
    }

    protected boolean canGenerateTestsForSUT() {
        if (TestCluster.getInstance().getNumTestCalls() == 0) {
            final InstrumentingClassLoader cl = TestGenerationContext.getInstance().getClassLoaderForSUT();
            final int numMethods = CFGMethodAdapter.getNumMethods(cl);
            return !(Properties.P_REFLECTION_ON_PRIVATE <= 0.0) && numMethods != 0;
        }
        return true;
    }
}
