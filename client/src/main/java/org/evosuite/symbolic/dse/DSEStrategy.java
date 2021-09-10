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
import org.evosuite.coverage.FitnessFunctionsUtils;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.result.TestGenerationResultBuilder;
import org.evosuite.rmi.ClientServices;
import org.evosuite.rmi.service.ClientState;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.strategy.TestGenerationStrategy;
import org.evosuite.symbolic.dse.algorithm.DSEAlgorithmFactory;
import org.evosuite.symbolic.dse.algorithm.DSEAlgorithms;
import org.evosuite.symbolic.dse.algorithm.ExplorationAlgorithm;
import org.evosuite.symbolic.dse.algorithm.listener.StoppingConditionFactory;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.evosuite.utils.ArrayUtil;
import org.evosuite.utils.LoggingUtils;
import org.evosuite.utils.Randomness;

import java.util.List;

/**
 * <p>
 * DSEStrategy class.
 * </p>
 * <p>
 * NOTE (ilebrero): Even though we are on evosuite, this module is a bit out of context with the GA general framework.
 * In the future may be a good idea to rebuild it as a standalone library.
 *
 * @author ignacio lebrero
 */
public class DSEStrategy extends TestGenerationStrategy {

    public static final String WITH_TIMEOUT = "* With timeout: {}";
    public static final String USING_DSE_ALGORITHM = "* Using DSE algorithm: {}";
    public static final String WITH_TARGET_COVERAGE = "* With target coverage: {}";
    public static final String SYMBOLIC_ARRAYS_SUPPORT_ENABLED = "* Symbolic arrays support enabled: {}";
    public static final String SETTING_UP_DSE_GENERATION_INFO_MESSAGE = "* Setting up DSE test suite generation";
    public static final String NOT_SUITABLE_METHOD_FOUND_INFO_MESSAGE = "* Found no testable methods in the target class {}";
    public static final String SYMBOLIC_ARRAYS_IMPLEMENTATION_SELECTED = "* Symbolic arrays implementation selected: {}";

    /**
     * Default stopping conditions
     */
    public static Properties.DSEStoppingConditionCriterion[] defaultStoppingConditions = {
            Properties.DSEStoppingConditionCriterion.MAXTIME,
            Properties.DSEStoppingConditionCriterion.ZEROFITNESS,
            Properties.DSEStoppingConditionCriterion.TARGETCOVERAGE
    };

    @Override
    public TestSuiteChromosome generateTests() {
        LoggingUtils.getEvoLogger().info(SETTING_UP_DSE_GENERATION_INFO_MESSAGE);
        Properties.CRITERION = Properties.DSE_EXPLORATION_ALGORITHM_TYPE.getCriteria();
        Criterion[] criterion = Properties.CRITERION;

        long startTime = System.currentTimeMillis() / 1000;

        List<TestFitnessFunction> goals = FitnessFunctionsUtils.getFitnessFunctionsGoals(criterion, true);
        if (!canGenerateTestsForSUT()) {
            LoggingUtils.getEvoLogger().info(
                    NOT_SUITABLE_METHOD_FOUND_INFO_MESSAGE,
                    Properties.TARGET_CLASS
            );
            ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Total_Goals, goals.size());

            return new TestSuiteChromosome();
        }

        /*
         * Proceed with search if CRITERION=EXCEPTION, even if goals is empty
         */
        TestSuiteChromosome testSuite;
        if (!(Properties.STOP_ZERO && goals.isEmpty())
                || ArrayUtil.contains(criterion, Criterion.EXCEPTION)) {
            // Perform search
            // This is in case any algorithm internal strategy uses some random behaviour.
            //     e.g. after x iterations selects the next path randomly.
            LoggingUtils.getEvoLogger().info("* Using seed {}", Randomness.getSeed());
            LoggingUtils.getEvoLogger().info("* Starting DSE");
            ClientServices.getInstance().getClientNode().changeState(ClientState.SEARCH);

            // Builds the actual algorithm
            ExplorationAlgorithm algorithm = buildDSEAlgorithm();

            // Logs enabled features
            logDSEEngineEnabledFeatures();

            testSuite = algorithm.explore();

            if (Properties.SERIALIZE_DSE || Properties.CLIENT_ON_THREAD) {
                TestGenerationResultBuilder.getInstance().setDSEAlgorithm(algorithm);
            }
        } else {
            testSuite = setNoGoalsCoverage(Properties.DSE_EXPLORATION_ALGORITHM_TYPE);
        }

        long endTime = System.currentTimeMillis() / 1000;

        goals = FitnessFunctionsUtils.getFitnessFunctionsGoals(criterion, false); // recalculated now after the search, eg to
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
                    .info(new StringBuilder()
                            .append("* Search finished after ")
                            .append(endTime - startTime)
                            .append("s, fitness: ")
                            .append(testSuite.getFitness())
                            .append(" and coverage: ")
                            .append(testSuite.getCoverage()).toString());
        }

        // Search is finished, send statistics
        sendExecutionStatistics();

        return testSuite;

    }

    private void logDSEEngineEnabledFeatures() {
        LoggingUtils.getEvoLogger().info(
                SYMBOLIC_ARRAYS_SUPPORT_ENABLED,
                Properties.IS_DSE_ARRAYS_SUPPORT_ENABLED);

        if (Properties.IS_DSE_ARRAYS_SUPPORT_ENABLED) {
            LoggingUtils.getEvoLogger().info(
                    SYMBOLIC_ARRAYS_IMPLEMENTATION_SELECTED,
                    Properties.SELECTED_DSE_ARRAYS_MEMORY_MODEL_VERSION.toString()
            );
        }
    }

    private ExplorationAlgorithm buildDSEAlgorithm() {
        DSEAlgorithmFactory dseFactory = new DSEAlgorithmFactory();
        DSEAlgorithms dseAlgorithmType = Properties.DSE_EXPLORATION_ALGORITHM_TYPE;

        LoggingUtils.getEvoLogger().info(USING_DSE_ALGORITHM, dseAlgorithmType.getName());
        ExplorationAlgorithm algorithm = dseFactory.getDSEAlgorithm(dseAlgorithmType);

        if (Properties.DSE_STOPPING_CONDITION.equals(Properties.DSEStoppingConditionCriterion.DEFAULTS)) {
            /** Default conditions */
            for (Properties.DSEStoppingConditionCriterion condition : defaultStoppingConditions) {
                algorithm.addStoppingCondition(StoppingConditionFactory.getStoppingCondition(condition));
            }

            /** Stopping conditions */
            for (Properties.DSEStoppingConditionCriterion stoppingConditionCriterion : dseAlgorithmType.getStoppingConditionCriterions()) {
                algorithm.addStoppingCondition(StoppingConditionFactory.getStoppingCondition(stoppingConditionCriterion));
            }
        } else {
            /** User chosen Stopping Condition */
            algorithm.addStoppingCondition(StoppingConditionFactory.getStoppingCondition(Properties.DSE_STOPPING_CONDITION));
        }


        /** Fitness functions */
        List<TestSuiteFitnessFunction> fitnessFunctions = FitnessFunctionsUtils.getFitnessFunctions(dseAlgorithmType.getCriteria());
        algorithm.addFitnessFunctions(fitnessFunctions);


        LoggingUtils.getEvoLogger().debug(WITH_TIMEOUT, Properties.GLOBAL_TIMEOUT);
        LoggingUtils.getEvoLogger().debug(WITH_TARGET_COVERAGE, Properties.DSE_TARGET_COVERAGE);

        return algorithm;
    }

    private TestSuiteChromosome setNoGoalsCoverage(DSEAlgorithms algorithm) {
        TestSuiteChromosome testSuite = new TestSuiteChromosome();
        List<TestSuiteFitnessFunction> fitnessFunctions = FitnessFunctionsUtils
                .getFitnessFunctions(algorithm.getCriteria());

        zeroFitness.setFinished();

        for (FitnessFunction<TestSuiteChromosome> ff : fitnessFunctions) {
            testSuite.setCoverage(ff, 1.0);
        }
        return testSuite;
    }


}
