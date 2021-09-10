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
package org.evosuite.symbolic.dse;

import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.coverage.TestFitnessFactory;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;
import org.evosuite.ga.stoppingconditions.StoppingCondition;
import org.evosuite.result.TestGenerationResultBuilder;
import org.evosuite.rmi.ClientServices;
import org.evosuite.rmi.service.ClientState;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.strategy.TestGenerationStrategy;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.evosuite.utils.ArrayUtil;
import org.evosuite.utils.LoggingUtils;
import org.evosuite.utils.Randomness;

import java.util.ArrayList;
import java.util.List;

public class DSEStrategyLegacy extends TestGenerationStrategy {

    @Override
    public TestSuiteChromosome generateTests() {
        LoggingUtils.getEvoLogger().info("* Setting up DSE test suite generation");

        long startTime = System.currentTimeMillis() / 1000;

        Properties.CRITERION = new Criterion[]{Properties.Criterion.BRANCH};

        // What's the search target
        List<TestSuiteFitnessFunction> fitnessFunctions = getFitnessFunctions();

        List<TestFitnessFunction> goals = getGoals(true);
        if (!canGenerateTestsForSUT()) {
            LoggingUtils.getEvoLogger()
                    .info("* Found no testable methods in the target class " + Properties.TARGET_CLASS);
            ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Total_Goals, goals.size());

            return new TestSuiteChromosome();
        }

        /*
         * Proceed with search if CRITERION=EXCEPTION, even if goals is empty
         */
        TestSuiteChromosome testSuite = null;
        if (!(Properties.STOP_ZERO && goals.isEmpty())
                || ArrayUtil.contains(Properties.CRITERION, Criterion.EXCEPTION)) {
            // Perform search
            LoggingUtils.getEvoLogger().info("* Using seed {}", Randomness.getSeed());
            LoggingUtils.getEvoLogger().info("* Starting evolution");
            ClientServices.getInstance().getClientNode().changeState(ClientState.SEARCH);

            DSELegacyAlgorithm algorithm = new DSELegacyAlgorithm();
            StoppingCondition<TestSuiteChromosome> stoppingCondition = getStoppingCondition();
            algorithm.addFitnessFunctions(fitnessFunctions);
            if (Properties.STOP_ZERO) {
                // TODO(ilebrero): what is this supposed to do?
            }
            algorithm.setStoppingCondition(stoppingCondition);
            algorithm.generateSolution();
            testSuite = algorithm.getBestIndividual();

            if (Properties.SERIALIZE_GA || Properties.CLIENT_ON_THREAD) {
                TestGenerationResultBuilder.getInstance().setGeneticAlgorithm(algorithm);
            }

        } else {
            zeroFitness.setFinished();
            testSuite = new TestSuiteChromosome();
            for (FitnessFunction<TestSuiteChromosome> ff : fitnessFunctions) {
                testSuite.setCoverage(ff, 1.0);
            }
        }

        long endTime = System.currentTimeMillis() / 1000;

        goals = getGoals(false); // recalculated now after the search, eg to
        // handle exception fitness
        ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Total_Goals, goals.size());

        // Newline after progress bar
        if (Properties.SHOW_PROGRESS)
            LoggingUtils.getEvoLogger().info("");

        if (!Properties.IS_RUNNING_A_SYSTEM_TEST) { // avoid printing time
            // related info in system
            // tests due to lack of
            // determinism
            LoggingUtils.getEvoLogger()
                    .info("* Search finished after " + (endTime - startTime) + "s and "
                            + MaxStatementsStoppingCondition.getNumExecutedStatements()
                            + " statements, best individual has fitness: " + testSuite.getFitness());
        }

        // Search is finished, send statistics
        sendExecutionStatistics();

        return testSuite;

    }

    private List<TestFitnessFunction> getGoals(boolean verbose) {
        List<TestFitnessFactory<? extends TestFitnessFunction>> goalFactories = getFitnessFactories();
        List<TestFitnessFunction> goals = new ArrayList<>();

        if (goalFactories.size() == 1) {
            TestFitnessFactory<? extends TestFitnessFunction> factory = goalFactories.iterator().next();
            goals.addAll(factory.getCoverageGoals());

            if (verbose) {
                LoggingUtils.getEvoLogger().info("* Total number of test goals: {}", factory.getCoverageGoals().size());
                if (Properties.PRINT_GOALS) {
                    for (TestFitnessFunction goal : factory.getCoverageGoals())
                        LoggingUtils.getEvoLogger().info("" + goal.toString());
                }
            }
        } else {
            if (verbose) {
                LoggingUtils.getEvoLogger().info("* Total number of test goals: ");
            }

            for (TestFitnessFactory<? extends TestFitnessFunction> goalFactory : goalFactories) {
                goals.addAll(goalFactory.getCoverageGoals());

                if (verbose) {
                    LoggingUtils.getEvoLogger()
                            .info("  - " + goalFactory.getClass().getSimpleName().replace("CoverageFactory", "") + " "
                                    + goalFactory.getCoverageGoals().size());
                    if (Properties.PRINT_GOALS) {
                        for (TestFitnessFunction goal : goalFactory.getCoverageGoals())
                            LoggingUtils.getEvoLogger().info("" + goal.toString());
                    }
                }
            }
        }
        return goals;
    }

}
