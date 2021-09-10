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
package org.evosuite.symbolic.dse.algorithm;

import org.evosuite.symbolic.PathCondition;
import org.evosuite.symbolic.PathConditionUtils;
import org.evosuite.symbolic.dse.DSEStatistics;
import org.evosuite.symbolic.dse.DSETestCase;
import org.evosuite.symbolic.dse.algorithm.listener.StoppingCondition;
import org.evosuite.testcase.TestCase;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Abstract superclass of DSE exploration algorithms
 *
 * @author Ignacio Lebrero
 */
public abstract class ExplorationAlgorithmBase implements Serializable {

    /**
     * Logger Messages
     */
    public static final String PATH_DIVERGENCE_FOUND_WARNING_MESSAGE = "Warning | Path condition diverged";
    public static final String SETTING_STOPPING_CONDITION_DEBUG_MESSAGE = "Setting stopping condition";
    public static final String ADDING_NEW_STOPPING_CONDITION_DEBUG_MESSAGE = "Adding new stopping condition";
    public static final String FITNESS_AFTER_ADDING_NEW_TEST_DEBUG_MESSAGE = "Fitness after adding new test: {}";
    public static final String FITNESS_BEFORE_ADDING_NEW_TEST__DEBUG_MESSAGE = "Fitness before adding new test: {}";
    public static final String NEW_TEST_GENERATED_IMPROVES_FITNESS_INFO_MESSAGE = "New test generated improves fitness";
    public static final String NEW_TEST_GENERATED_DIDNT_IMPROVES_FITNESS_INFO_MESSAGE = "New test generated doesn't improves fitness";
    public static final String CALCULATING_FITNESS_FOR_CURRENT_TEST_SUITE_DEBUG_MESSAGE = "Calculating fitness for current test suite";
    public static final String ABOUT_TO_ADD_A_NEW_TEST_CASE_TO_THE_TEST_SUITE_DEBUG_MESSAGE = "About to add a new testCase to the test suite";

    // TODO: this values can be moved to a general ExplorationAlgorithmConfig object later on
    public static final long NORMALIZE_VALUE_LIMIT = 100;
    public static final boolean SHOW_PROGRESS_DEFAULT_VALUE = false;

    /**
     * Path Divergence config
     */
    public static final int PATH_DIVERGED_BASED_TEST_CASE_PENALTY_SCORE = 0;

    /**
     * Test suite
     */
    protected final TestSuiteChromosome testSuite = new TestSuiteChromosome();

    /**
     * Fitness Functions for calculating coverage
     */
    protected List<TestSuiteFitnessFunction> fitnessFunctions = new ArrayList();

    /**
     * List of conditions on which to end the search
     */
    protected transient Set<StoppingCondition> stoppingConditions = new HashSet();

    /**
     * DSE statistics
     */
    protected transient final DSEStatistics statisticsLogger;

    protected final boolean showProgress;

    private static final transient Logger logger = LoggerFactory.getLogger(ExplorationAlgorithmBase.class);

    private static final long serialVersionUID = -3426910907322781226L;

    public ExplorationAlgorithmBase(DSEStatistics dseStatistics, boolean showProgress) {
        this.showProgress = showProgress;
        this.statisticsLogger = dseStatistics;
    }

    /**
     * Exploration entry point.
     */
    public abstract TestSuiteChromosome explore();

    /**
     * Add new fitness function
     *
     * @param function
     */
    public void addFitnessFunction(TestSuiteFitnessFunction function) {
        fitnessFunctions.add(function);
    }

    /**
     * Add new fitness functions
     *
     * @param functions
     */
    public void addFitnessFunctions(List<TestSuiteFitnessFunction> functions) {
        for (TestSuiteFitnessFunction function : functions)
            this.addFitnessFunction(function);
    }

    /**
     * Get currently used fitness function
     *
     * @return
     */
    public TestSuiteFitnessFunction getFitnessFunction() {
        return fitnessFunctions.get(0);
    }

    /**
     * Get all used fitness function
     *
     * @return
     */
    public List<TestSuiteFitnessFunction> getFitnessFunctions() {
        return fitnessFunctions;
    }

    /**
     * Calculates current test suite fitness
     */
    public void calculateFitness() {
        logger.debug(CALCULATING_FITNESS_FOR_CURRENT_TEST_SUITE_DEBUG_MESSAGE);

        for (TestSuiteFitnessFunction fitnessFunction : fitnessFunctions) {
            fitnessFunction.getFitness(testSuite);
        }
    }

    /**
     * Calculates current test suite fitness
     */
    public double getFitness() {
        return testSuite.getFitness();
    }

    /**
     * <p>
     * addStoppingCondition
     * </p>
     *
     * @param condition a {@link org.evosuite.ga.stoppingconditions.StoppingCondition}
     *                  object.
     */
    public void addStoppingCondition(StoppingCondition condition) {
        Iterator<StoppingCondition> it = stoppingConditions.iterator();
        while (it.hasNext()) {
            if (it.next().getClass().equals(condition.getClass())) {
                return;
            }
        }
        logger.debug(ADDING_NEW_STOPPING_CONDITION_DEBUG_MESSAGE);
        stoppingConditions.add(condition);
    }

    /**
     * Getter for the stopping conditions.
     *
     * @return Set of stopping conditions
     */
    public Set<StoppingCondition> getStoppingConditions() {
        return stoppingConditions;
    }

    // TODO: Override equals method in StoppingCondition

    /**
     * <p>
     * setStoppingCondition
     * </p>
     *
     * @param condition a {@link org.evosuite.ga.stoppingconditions.StoppingCondition}
     *                  object.
     */
    public void setStoppingCondition(StoppingCondition condition) {
        stoppingConditions.clear();
        logger.debug(SETTING_STOPPING_CONDITION_DEBUG_MESSAGE);
        stoppingConditions.add(condition);
    }

    /**
     * <p>
     * removeStoppingCondition
     * </p>
     *
     * @param condition a {@link org.evosuite.ga.stoppingconditions.StoppingCondition}
     *                  object.
     */
    public void removeStoppingCondition(StoppingCondition condition) {
        Iterator<StoppingCondition> it = stoppingConditions.iterator();
        while (it.hasNext()) {
            if (it.next().getClass().equals(condition.getClass())) {
                it.remove();
            }
        }
    }

    /**
     * <p>
     * resetStoppingConditions
     * </p>
     */
    public void resetStoppingConditions() {
        for (StoppingCondition c : stoppingConditions) {
            c.reset();
        }
    }

    /**
     * Determine whether any of the stopping conditions hold
     *
     * @return a boolean.
     */
    public boolean isFinished() {
        for (StoppingCondition c : stoppingConditions) {
            if (c.isFinished())
                return true;
        }
        return false;
    }

    /**
     * Getter for the generated test suite.
     *
     * @return testSuite
     */
    public TestSuiteChromosome getGeneratedTestSuite() {
        return testSuite;
    }

    /**
     * Notify all search listeners of iteration
     */
    protected void notifyIteration() {
        for (StoppingCondition stoppingCondition : stoppingConditions) {
            stoppingCondition.iteration(this);
        }
    }

    /**
     * Notify all search listeners of search start
     */
    protected void notifyGenerationStarted() {
        for (StoppingCondition stoppingCondition : stoppingConditions) {
            stoppingCondition.generationStarted(this);
        }
    }

    /**
     * Notify all stopping conditions of search end
     */
    protected void notifyGenerationFinished() {
        for (StoppingCondition stoppingCondition : stoppingConditions) {
            stoppingCondition.generationFinished(this);
        }
    }


    /**
     * Returns the progress of the search.
     *
     * @return a value [0.0, 1.0]
     */
    protected double getProgress() {
        long totalbudget = 0;
        double currentbudget = 0;

        for (StoppingCondition sc : this.stoppingConditions) {
            if (sc.getLimit() != 0) {
                totalbudget += NORMALIZE_VALUE_LIMIT;
                currentbudget += getNormalizedValue(sc.getCurrentValue(), sc.getLimit());
            }
        }

        return currentbudget / (double) totalbudget;
    }

    /**
     * Checks whether the current executed path condition diverged from the original one.
     * TODO:
     * 			1) Maybe we can give some info about the PathCondition that diverged later on
     * 			2) Move this to PathConditionUtils
     *
     * @param currentPathCondition
     */
    protected boolean checkPathConditionDivergence(PathCondition currentPathCondition, PathCondition expectedPathCondition) {
        boolean hasPathConditionDiverged = PathConditionUtils.hasPathConditionDiverged(expectedPathCondition, currentPathCondition);

        if (hasPathConditionDiverged) {
            logger.debug(PATH_DIVERGENCE_FOUND_WARNING_MESSAGE);
            statisticsLogger.reportNewPathDivergence();
        }

        return hasPathConditionDiverged;
    }

    /**
     * Returns the incremental coverage of adding a test case to the current test suite.
     *
     * @param newTestCase
     * @return
     */
    protected double getTestCaseAdditionIncrementalCoverage(TestCase newTestCase) {
        double oldCoverage;
        double newCoverage;
        double coverageDiff;

        // New coverage calculation
        testSuite.addTest(newTestCase);
        calculateFitness();

        newCoverage = testSuite.getCoverage();

        // Restore old values and calculate old coverage
        testSuite.deleteTest(newTestCase);
        calculateFitness();

        oldCoverage = testSuite.getCoverage();
        coverageDiff = newCoverage - oldCoverage;

        logNewTestCoverageData(coverageDiff);

        return coverageDiff;
    }

    /**
     * Prints old/new fitness values and adds the new test case.
     *
     * @param dseTestCase
     */
    protected void addNewTestCaseToTestSuite(DSETestCase dseTestCase) {
        logger.debug(ABOUT_TO_ADD_A_NEW_TEST_CASE_TO_THE_TEST_SUITE_DEBUG_MESSAGE);
        logger.debug(FITNESS_BEFORE_ADDING_NEW_TEST__DEBUG_MESSAGE, testSuite.getFitness());

        testSuite.addTest(dseTestCase.getTestCase());
        calculateFitness();

        logger.debug(FITNESS_AFTER_ADDING_NEW_TEST_DEBUG_MESSAGE, testSuite.getFitness());
    }

    /**
     * Symbolic algorithm general schema.
     *
     * @param method
     */
    protected abstract void explore(Method method);

    /**
     * Logs new coverage data.
     *
     * @param coverageDiff
     */
    private void logNewTestCoverageData(double coverageDiff) {
        if (coverageDiff > 0) {
            logger.debug(NEW_TEST_GENERATED_IMPROVES_FITNESS_INFO_MESSAGE);
            statisticsLogger.reportNewTestUseful();
        } else {
            logger.debug(NEW_TEST_GENERATED_DIDNT_IMPROVES_FITNESS_INFO_MESSAGE);
            statisticsLogger.reportNewTestUnuseful();
        }
    }

    /**
     * Each stopping condition can have diferent limits, thus we normalize the values to just get the
     * correspondent "percentage" related to the limit it had.
     *
     * @param currentValue
     * @param limit
     * @return
     */
    private double getNormalizedValue(long currentValue, long limit) {
        return (double) (currentValue * NORMALIZE_VALUE_LIMIT) / (double) limit;
    }
}