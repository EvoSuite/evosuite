/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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
/**
 * 
 */
package org.evosuite.testsuite.localsearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.localsearch.LocalSearchBudget;
import org.evosuite.ga.localsearch.LocalSearchObjective;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the fitness functions (the objective) for a whole test suite such that
 * we will apply local search to a single test case within the whole test suite.
 * The <code>TestSuiteLocalSearchObjective</code> has an internal reference to the whole test suite 
 * and an index to the position of the test case that is being modified by means of local search.
 * 
 * @author Gordon Fraser
 */
public class TestSuiteLocalSearchObjective implements LocalSearchObjective<TestChromosome> {

	private static final Logger logger = LoggerFactory.getLogger(TestSuiteLocalSearchObjective.class);

	private final List<TestSuiteFitnessFunction> fitnessFunctions = new ArrayList<>();

	private final TestSuiteChromosome suite;

	private final int testIndex;

	// TODO: This assumes we are not doing NSGA-II
	private boolean isMaximization = false;

	private double lastFitnessSum = 0.0;

	private Map<FitnessFunction<?>, Double> lastFitness = new HashMap<>();

	private Map<FitnessFunction<?>, Double> lastCoverage = new HashMap<>();

	/**
	 * Creates a Local Search objective for a TestCase that will be optimized
	 * using a containing TestSuite to measure the changes in fitness values.
	 * 
	 * @param fitness
	 *            the list of fitness functions to use to compute the fitness of
	 *            the TestSuiteChromosome
	 * @param suite
	 *            a TestSuite chromosome that will be subjected to local search
	 * @param index
	 *            a test index (between 0 and the test suite length) that will
	 *            be used to modify the testchromosome each time a changed has
	 *            been applied.
	 */
	private TestSuiteLocalSearchObjective(List<TestSuiteFitnessFunction> fitness, TestSuiteChromosome suite,
			int index) {
		this.fitnessFunctions.addAll(fitness);
		this.suite = suite;
		this.testIndex = index;
		for (TestSuiteFitnessFunction ff : fitness) {
			if (ff.isMaximizationFunction())
				isMaximization = true;
			else
				isMaximization = false;
			break;
		}
		updateLastFitness();
		updateLastCoverage();
	}

	/**
	 * Creates a new <code>TestSuiteLocalSearchObjective</code> for a given list
	 * of fitness functions, a test suite and a <code>testIndex</code> for
	 * replacing an optimised test case (i.e. a test case over which was applied
	 * local search)
	 * 
	 * @param fitness
	 *            the list of fitness functions to be used on the modified test
	 *            suite
	 * @param suite
	 *            the original test suite
	 * @param index
	 *            the index (between 0 and the suite length) that will be
	 *            replaced with a new test case
	 * @return
	 */
	public static TestSuiteLocalSearchObjective buildNewTestSuiteLocalSearchObjective(
			List<FitnessFunction<? extends Chromosome>> fitness, TestSuiteChromosome suite, int index) {
		List<TestSuiteFitnessFunction> ffs = new ArrayList<>();
		for (FitnessFunction<? extends Chromosome> ff : fitness) {
			TestSuiteFitnessFunction tff = (TestSuiteFitnessFunction) ff;
			ffs.add(tff);
		}
		return new TestSuiteLocalSearchObjective(ffs, suite, index);
	}

	private void updateLastFitness() {
		lastFitnessSum = 0.0;
		for (TestSuiteFitnessFunction fitness : fitnessFunctions) {
			double newFitness = fitness.getFitness(suite);
			lastFitnessSum += newFitness;
			lastFitness.put(fitness, newFitness);
		}
	}

	private void updateLastCoverage() {
		for (TestSuiteFitnessFunction fitness : fitnessFunctions) {
			lastCoverage.put(fitness, suite.getCoverage(fitness));
		}
	}

	/**
	 * Returns true if all the fitness functions are minimising and the fitness
	 * value for each of them is 0.0
	 */
	@Override
	public boolean isDone() {

		for (TestSuiteFitnessFunction fitness : fitnessFunctions) {
			if (fitness.isMaximizationFunction() || fitness.getFitness(suite) != 0.0)
				return false;
		}
		return true;
	}

	/**
	 * Replaces the test case at position <code>testIndex</code> with the passed
	 * TestChromosome.
	 */
	@Override
	public boolean hasImproved(TestChromosome testCase) {
		return hasChanged(testCase) < 0;
	}

	/**
	 * Returns true if by replacing the test case at position
	 * <code>testIndex</code> with the argument <code>testCase</code>, the
	 * resulting test suite has not worsened the fitness
	 */
	@Override
	public boolean hasNotWorsened(TestChromosome testCase) {
		return hasChanged(testCase) < 1;
	}

	private boolean isFitnessBetter(double newFitness, double oldFitness) {
		if (isMaximization) {
			return newFitness > oldFitness;
		} else {
			return newFitness < oldFitness;
		}
	}

	private boolean isFitnessWorse(double newFitness, double oldFitness) {
		if (isMaximization) {
			return newFitness < oldFitness;
		} else {
			return newFitness > oldFitness;
		}
	}

	/**
	 * Overrides the test case at position <code>testIndex</code> with the
	 * individual. It returns <code>-1</code> if the new fitness has improved,
	 * <code>1</code> if the fitness has worsened or <code>0</code> if the
	 * fitness has not changed.
	 */
	@Override
	public int hasChanged(TestChromosome testCase) {
		testCase.setChanged(true);
		suite.setTestChromosome(testIndex, testCase);
		LocalSearchBudget.getInstance().countFitnessEvaluation();
		for (TestSuiteFitnessFunction fitnessFunction : fitnessFunctions)
			fitnessFunction.getFitness(suite);
		double newFitness = suite.getFitness();

		if (isFitnessBetter(newFitness, lastFitnessSum)) {
			logger.info("Local search improved fitness from " + lastFitnessSum + " to " + newFitness);
			updateLastFitness();
			updateLastCoverage();
			return -1;
		} else if (isFitnessWorse(newFitness, lastFitnessSum)) {
			logger.info("Local search worsened fitness from " + lastFitnessSum + " to " + newFitness);
			suite.setFitnessValues(lastFitness);
			suite.setCoverageValues(lastCoverage);
			return 1;
		} else {
			logger.info("Local search did not change fitness of " + lastFitnessSum);

			updateLastCoverage();
			return 0;
		}
	}

	/**
	 * Since the fitness is computed by the underlying test suite associated to
	 * this local search objective, this function should not belong. TODO: Why
	 * not simply returning the fitness functions of the suite?
	 */
	@Override
	public List<FitnessFunction<? extends Chromosome>> getFitnessFunctions() {
		throw new NotImplementedException("This should not be called");
	}

	/**
	 * Since the fitness is governed by the underlying suite associated to this
	 * goal, this function should never be invoked.
	 */
	@Override
	public void addFitnessFunction(FitnessFunction<? extends Chromosome> fitness) {
		throw new NotImplementedException("This should not be called");
	}

	@Override
	public boolean isMaximizationObjective() {
		return isMaximization;
	}

}
