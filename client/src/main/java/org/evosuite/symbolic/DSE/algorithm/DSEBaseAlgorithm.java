/**
 * Copyright (C) 2010-2020 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.symbolic.DSE.algorithm;

import org.evosuite.ga.Chromosome;
import org.evosuite.symbolic.DSE.DSEStatistics;
import org.evosuite.symbolic.DSE.DSETestCase;
import org.evosuite.symbolic.DSE.algorithm.listener.StoppingCondition;
import org.evosuite.symbolic.PathCondition;
import org.evosuite.symbolic.PathConditionUtils;
import org.evosuite.testcase.TestCase;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Abstract superclass of DSE algorithms
 *
 * @author Ignacio Lebrero
 */
public abstract class DSEBaseAlgorithm<T extends Chromosome> implements Serializable {

	private static final long serialVersionUID = -3426910907322781226L;

	/** Logger Messages */
	public static final String PATH_DIVERGENCE_FOUND_WARNING_MESSAGE = "Warning | Path condition diverged";
	public static final String SETTING_STOPPING_CONDITION_DEBUG_MESSAGE = "Setting stopping condition";
	public static final String ADDING_NEW_STOPPING_CONDITION_DEBUG_MESSAGE = "Adding new stopping condition";
	public static final String FITNESS_AFTER_ADDING_NEW_TEST_DEBUG_MESSAGE = "Fitness after adding new test: {}";
	public static final String FITNESS_BEFORE_ADDING_NEW_TEST__DEBUG_MESSAGE = "Fitness before adding new test: {}";
	public static final String CALCULATING_FITNESS_FOR_CURRENT_TEST_SUITE_DEBUG_MESSAGE = "Calculating fitness for current test suite";
	public static final String ABOUT_TO_ADD_A_NEW_TEST_CASE_TO_THE_TEST_SUITE_DEBUG_MESSAGE = "About to add a new testCase to the test suite";

	public static final int PATH_DIVERGED_BASED_TEST_CASE_PENALTY_SCORE = 0;

	private static final Logger logger = LoggerFactory.getLogger(DSEBaseAlgorithm.class);

	protected final TestSuiteChromosome testSuite = new TestSuiteChromosome();

	/** Fitness Functions */
	protected List<TestSuiteFitnessFunction> fitnessFunctions = new ArrayList();

	/** List of conditions on which to end the search */
	protected transient Set<StoppingCondition> stoppingConditions = new HashSet();

	/** DSE statistics */
	protected transient final DSEStatistics statisticsLogger;

	public DSEBaseAlgorithm(DSEStatistics dseStatistics) {
		this.statisticsLogger = dseStatistics;
	}

	/**
	 * Add new fitness function (i.e., for new mutation)
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
	 * @return
	 */
	public TestSuiteFitnessFunction getFitnessFunction() {
		return fitnessFunctions.get(0);
	}

	/**
	 * Get all used fitness function
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
	 * @param condition
	 *            a {@link org.evosuite.ga.stoppingconditions.StoppingCondition}
	 *            object.
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

	public Set<StoppingCondition> getStoppingConditions() {
		return stoppingConditions;
	}

	// TODO: Override equals method in StoppingCondition
	/**
	 * <p>
	 * setStoppingCondition
	 * </p>
	 *
	 * @param condition
	 *            a {@link org.evosuite.ga.stoppingconditions.StoppingCondition}
	 *            object.
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
	 * @param condition
	 *            a {@link org.evosuite.ga.stoppingconditions.StoppingCondition}
	 *            object.
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
	protected double progress() {
		long totalbudget = 0;
		long currentbudget = 0;

		for (StoppingCondition sc : this.stoppingConditions) {
			if (sc.getLimit() != 0) {
				totalbudget += sc.getLimit();
				currentbudget += sc.getCurrentValue();
			}
		}

		return (double) currentbudget / (double) totalbudget;
	}

	public TestSuiteChromosome getGeneratedTestSuite() {
		return testSuite;
	}
	
    /**
     * Checks whether the current executed path condition diverged from the original one.
     * TODO: Maybe we can give some info about the PathCondition that diverged later on
	 *
     * @param currentPathCondition
     */
    protected boolean checkPathConditionDivergence(PathCondition currentPathCondition, PathCondition expectedPathCondition) {
		boolean hasPathConditionDiverged = PathConditionUtils.hasPathConditionDiverged(expectedPathCondition, currentPathCondition);
    	statisticsLogger.reportNewPathExplored();

        if (hasPathConditionDiverged) {
            logger.debug(PATH_DIVERGENCE_FOUND_WARNING_MESSAGE);
        	statisticsLogger.reportNewPathDivergence();
        }

        return hasPathConditionDiverged;
    }

	/**
	 * Score calculation is based on fitness improvement against the current testSuite.
	 *
	 * @param newTestCase
	 * @param hasPathConditionDiverged
	 * @return
	 */
    protected double getTestScore(TestCase newTestCase, boolean hasPathConditionDiverged) {
    	// In case of divergence we add the worst score that there could be
    	if (hasPathConditionDiverged) return PATH_DIVERGED_BASED_TEST_CASE_PENALTY_SCORE;

    	double oldCoverage;
    	double newCoverage;

    	oldCoverage = testSuite.getCoverage();

		testSuite.addTest(newTestCase);
        calculateFitness();

        newCoverage = testSuite.getCoverage();

		testSuite.deleteTest(newTestCase);
		calculateFitness();

		return newCoverage - oldCoverage;
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
    protected abstract void runAlgorithm(Method method);

}