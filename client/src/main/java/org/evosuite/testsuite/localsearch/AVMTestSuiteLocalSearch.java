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
package org.evosuite.testsuite.localsearch;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.localsearch.LocalSearchBudget;
import org.evosuite.ga.localsearch.LocalSearchObjective;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.localsearch.AVMTestCaseLocalSearch;
import org.evosuite.testcase.localsearch.TestCaseLocalSearch;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.evosuite.utils.Randomness;

/**
 * Applies the AVM local search described in
 * http://dx.doi.org/10.1016/j.jss.2014.05.032 at the suite level
 * 
 * @author galeotti
 *
 */
public class AVMTestSuiteLocalSearch extends TestSuiteLocalSearch {

	/**
	 * Applies local search to the current individual targeting the objective
	 * passed as parameter
	 */
	@Override
	public boolean doSearch(TestSuiteChromosome suite, LocalSearchObjective<TestSuiteChromosome> localSearchObjective) {

		updateFitness(suite, localSearchObjective.getFitnessFunctions());
		double fitnessBefore = suite.getFitness();
		// logger.info("Test suite before local search: " + individual);

		List<TestChromosome> originalTests = new ArrayList<TestChromosome>(suite.getTestChromosomes());
		List<TestChromosome> tests = suite.getTestChromosomes();
		/*
		 * When we apply local search, due to budget constraints we might not be
		 * able to evaluate all the test cases in a test suite. When we apply LS
		 * several times on same individual in different generations, to avoid
		 * having always the same test cases searched for and others skipped,
		 * then we shuffle the test cases, so each time the order is different
		 */
		Randomness.shuffle(tests);

		if (Properties.LOCAL_SEARCH_ENSURE_DOUBLE_EXECUTION)
			ensureDoubleExecution(suite, localSearchObjective);

		if (Properties.LOCAL_SEARCH_RESTORE_COVERAGE)
			restoreBranchCoverage(suite, (TestSuiteFitnessFunction) localSearchObjective.getFitnessFunctions().get(0));

		if (Properties.LOCAL_SEARCH_EXPAND_TESTS)
			expandTestSuite(suite, localSearchObjective);

		doAVMSearchOnTestChromosomes(suite, localSearchObjective);

		LocalSearchBudget.getInstance().countLocalSearchOnTestSuite();

		// Fitness value may actually get worse if we are dealing with static
		// state.
		// As long as EvoSuite can't handle this, we cannot check this
		// assertion.
		// assert (objective.getFitnessFunction().isMaximizationFunction() ?
		// fitnessBefore <= individual.getFitness()
		// : fitnessBefore >= individual.getFitness()) : "Fitness was "
		// + fitnessBefore + " and now is " + individual.getFitness();

		// Return true if fitness has improved
		boolean hasImproved = hasImproved(fitnessBefore, suite, localSearchObjective);
		if (!hasImproved) {
			// restore original tests
			suite.clearTests();
			suite.addTests(originalTests);
		}
		return hasImproved;
	}

	/**
	 * Apply local search to each test case of this test suite using the
	 * objective as target for the local search. If local search was already
	 * applied to a test case, then randomize all primitives before applying
	 * local search again.
	 * 
	 * @param suite
	 * @param localSearchObjective
	 */
	private void doAVMSearchOnTestChromosomes(TestSuiteChromosome suite,
			LocalSearchObjective<TestSuiteChromosome> localSearchObjective) {
		List<TestChromosome> tests = suite.getTestChromosomes();
		for (int testIndex = 0; testIndex < tests.size(); testIndex++) {
			final TestChromosome test = tests.get(testIndex);

			// If we have already tried local search before on this test
			// without success, we reset all primitive values before trying
			// again
			if (test.hasLocalSearchBeenApplied()) {
				TestCaseLocalSearch.randomizePrimitives(test.getTestCase());
				updateFitness(suite, localSearchObjective.getFitnessFunctions());
			}

			if (LocalSearchBudget.getInstance().isFinished()) {
				logger.debug("Local search budget used up: " + Properties.LOCAL_SEARCH_BUDGET_TYPE);
				break;
			}
			logger.debug("Local search budget not yet used up");

			boolean improved;
			improved = applyLocalSearchOnTestCase(suite, testIndex, test, localSearchObjective);
			if (improved) {
				updateFitness(suite, localSearchObjective.getFitnessFunctions());
			} else {
				// fitness cannot be worse than before by construction
				// no need to update fitness
			}

			test.setLocalSearchApplied(true);
		}

	}

	/**
	 * Applies local search on the test case, using the suite fitness as a local search objective.
	 * 
	 * @param suite 
	 * @param testCaseIndex
	 * @param testCase
	 * @param localSearchObjective
	 * @return
	 */
	private static boolean applyLocalSearchOnTestCase(TestSuiteChromosome suite, int testCaseIndex, TestChromosome testCase,
			LocalSearchObjective<TestSuiteChromosome> localSearchObjective) {
		logger.debug("Local search on test " + testCaseIndex + ", current fitness: " + suite.getFitness());
		final List<FitnessFunction<? extends Chromosome>> fitnessFunctions = localSearchObjective.getFitnessFunctions();
		TestSuiteLocalSearchObjective testCaseLocalSearchObjective = TestSuiteLocalSearchObjective
				.buildNewTestSuiteLocalSearchObjective(fitnessFunctions, suite, testCaseIndex);

		TestCaseLocalSearch testCaselocalSearch = new AVMTestCaseLocalSearch();
		boolean improved = testCaselocalSearch.doSearch(testCase, testCaseLocalSearchObjective);
		return improved;
	}

}
