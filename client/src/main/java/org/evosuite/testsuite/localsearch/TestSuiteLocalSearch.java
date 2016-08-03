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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.localsearch.LocalSearch;
import org.evosuite.ga.localsearch.LocalSearchBudget;
import org.evosuite.ga.localsearch.LocalSearchObjective;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestCaseExpander;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.testcase.localsearch.AVMTestCaseLocalSearch;
import org.evosuite.testcase.localsearch.BranchCoverageMap;
import org.evosuite.testcase.localsearch.DSETestCaseLocalSearch;
import org.evosuite.testcase.localsearch.TestCaseLocalSearch;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * . This class applies local search on a test suite. Depending on the values
 * for properties <code>DSE_PROBABILITY</code> and <code>LOCAL_SEARCH_DSE</code>
 * one of the following three modes is applied:
 * 
 * - apply DSE on all test cases
 * 
 * - apply AVM on all test cases
 * 
 * - apply DSE on some tests and AVM on other tests
 * 
 * @author galeotti
 *
 */
public class TestSuiteLocalSearch implements LocalSearch<TestSuiteChromosome> {

	private static final Logger logger = LoggerFactory.getLogger(TestSuiteLocalSearch.class);

	/**
	 * Updates the given list of fitness functions using for the individual
	 * passed as a parameter
	 *
	 * @param individual
	 *            an individual
	 * 
	 * @param fitnessFunctions
	 *            the list of fitness functions to be updated
	 */
	private void updateFitness(TestSuiteChromosome individual,
			List<FitnessFunction<? extends Chromosome>> fitnessFunctions) {
		for (FitnessFunction<? extends Chromosome> ff : fitnessFunctions) {
			((TestSuiteFitnessFunction) ff).getFitness(individual);
		}
	}

	/**
	 * Decides the kind of local search that will be applied to the Test Suite.
	 * 
	 * @return a <code>TestSuiteLocalSearch</code> instance to use for local
	 *         search
	 */
	public static TestSuiteLocalSearch selectTestSuiteLocalSearch() {
		return new TestSuiteLocalSearch();
	}

	/**
	 * Before applying DSE we expand test cases, such that each primitive value
	 * is used at only exactly one position as a parameter
	 * 
	 * For example, given the following test case:
	 * 
	 * <code>
	 * foo0.bar(1);
	 * foo1.bar(1);
	 * </code>
	 * 
	 * is rewritten as:
	 * 
	 * <code>
	 * int int0 = 1;
	 * int int1 = 1;
	 * foo0.bar(int0);
	 * foo1.bar(int1);
	 * </code>
	 * 
	 * @param suite
	 * @return
	 */
	private static void expandTestSuite(TestSuiteChromosome suite,
			LocalSearchObjective<TestSuiteChromosome> objective) {
		logger.debug("Expanding tests for local search");

		TestSuiteChromosome newTestSuite = new TestSuiteChromosome();
		for (TestChromosome test : suite.getTestChromosomes()) {

			// First make sure we are up to date with the execution
			if (test.getLastExecutionResult() == null || test.isChanged()) {
				test.setLastExecutionResult(TestCaseExecutor.runTest(test.getTestCase()));
				test.setChanged(false);
			}

			// We skip tests that have problems
			if (test.getLastExecutionResult().hasTimeout() || test.getLastExecutionResult().hasTestException()) {
				logger.info("Skipping test with timeout or exception");
				continue;
			}

			// If local search has already been applied on the original test
			// then we also set that flag on the expanded test
			boolean hasLocalSearchBeenApplied = test.hasLocalSearchBeenApplied();
			TestCase newTest = test.getTestCase().clone();
			TestCase expandedTest = expandTestCase(newTest);
			TestChromosome expandedTestChromosome = newTestSuite.addTest(expandedTest);
			expandedTestChromosome.setLocalSearchApplied(hasLocalSearchBeenApplied);
		}
		List<TestChromosome> oldTests = suite.getTestChromosomes();
		oldTests.clear();
		oldTests.addAll(newTestSuite.getTestChromosomes());
		suite.setChanged(true);
		for (FitnessFunction<? extends Chromosome> ff : objective.getFitnessFunctions()) {
			((TestSuiteFitnessFunction) ff).getFitness(suite);
		}
	}

	/**
	 * Returns a new test case by explicitly declaring a variable for each used
	 * primitive value. Repeated values are declared as different variables. For
	 * example, given the following test case:
	 * 
	 * <code>
	 * foo0.bar(1);
	 * foo1.bar(1);
	 * </code>
	 * 
	 * is rewritten as:
	 * 
	 * <code>
	 * int int0 = 1;
	 * int int1 = 1;
	 * foo0.bar(int0);
	 * foo1.bar(int1);
	 * </code>
	 * 
	 * @param test
	 *            the test to expand
	 * @return the expanded test case
	 */
	private static TestCase expandTestCase(TestCase test) {
		if (!Properties.LOCAL_SEARCH_EXPAND_TESTS)
			return test;

		TestCaseExpander expander = new TestCaseExpander();
		return expander.expandTestCase(test);
	}

	/**
	 * Ensure that all branches are executed twice For each branch such that
	 * exists only one test case in the suite that covers that branch, it
	 * creates a duplicate of that test case.
	 * 
	 * By doing this, we avoid to incorrectly mark a new test case produced by
	 * the local search as an improving test case because it simply executes
	 * again a predicate.
	 */
	protected static void ensureDoubleExecution(TestSuiteChromosome individual,
			LocalSearchObjective<TestSuiteChromosome> objective) {
		logger.debug("Ensuring double execution");

		Set<TestChromosome> duplicates = new HashSet<TestChromosome>();
		TestSuiteFitnessFunction defaultFitness = (TestSuiteFitnessFunction) objective.getFitnessFunctions().get(0);

		Map<Integer, Integer> covered = new HashMap<Integer, Integer>();
		Map<Integer, TestChromosome> testMap = new HashMap<Integer, TestChromosome>();
		for (TestChromosome test : individual.getTestChromosomes()) {

			// Make sure we have an execution result
			if (test.getLastExecutionResult() == null || test.isChanged()) {
				ExecutionResult result = test.executeForFitnessFunction(defaultFitness);
				test.setLastExecutionResult(result); // .clone();
				test.setChanged(false);
			}

			for (Entry<Integer, Integer> entry : test.getLastExecutionResult().getTrace().getPredicateExecutionCount()
					.entrySet()) {
				if (!covered.containsKey(entry.getKey())) {
					covered.put(entry.getKey(), 0);
				}
				covered.put(entry.getKey(), covered.get(entry.getKey()) + entry.getValue());
				testMap.put(entry.getKey(), test);
			}
		}

		for (Entry<Integer, Integer> entry : covered.entrySet()) {
			int branchId = entry.getKey();
			int count = entry.getValue();
			if (count == 1) {
				TestChromosome duplicate = (TestChromosome) testMap.get(branchId).clone();
				ExecutionResult result = duplicate.executeForFitnessFunction(defaultFitness);
				duplicate.setLastExecutionResult(result); // .clone();
				duplicate.setChanged(false);
				duplicates.add(duplicate);
			}
		}

		if (!duplicates.isEmpty()) {
			logger.info("Adding " + duplicates.size() + " tests to cover branches sufficiently");
			for (TestChromosome test : duplicates) {
				individual.addTest(test);
			}
			individual.setChanged(true);
			for (FitnessFunction<? extends Chromosome> ff : objective.getFitnessFunctions()) {
				((TestSuiteFitnessFunction) ff).getFitness(individual);
			}
		}
	}

	/**
	 * Returns the set of predicate indexes whose true branches were covered by
	 * the suite
	 * 
	 * @param suite
	 * @return
	 */
	private static Set<Integer> getCoveredTrueBranches(TestSuiteChromosome suite) {
		Set<Integer> covered = new LinkedHashSet<Integer>();
		for (TestChromosome testChromosome : suite.getTestChromosomes()) {
			ExecutionResult lastResult = testChromosome.getLastExecutionResult();
			if (lastResult != null) {
				covered.addAll(lastResult.getTrace().getCoveredTrueBranches());
			}
		}
		return covered;
	}

	/**
	 * Returns the set of the predicate indexes whose false branch were covered
	 * by the test suite
	 * 
	 * @param suite
	 * @return the set of predicate indexes whose false branch were covered
	 */
	private static Set<Integer> getCoveredFalseBranches(TestSuiteChromosome suite) {
		Set<Integer> covered = new LinkedHashSet<Integer>();
		for (TestChromosome testChromosome : suite.getTestChromosomes()) {
			ExecutionResult lastResult = testChromosome.getLastExecutionResult();
			if (lastResult != null) {
				covered.addAll(lastResult.getTrace().getCoveredFalseBranches());
			}
		}
		return covered;
	}

	/**
	 * Ensure that all branches are executed twice
	 */
	private void restoreBranchCoverage(TestSuiteChromosome individual, TestSuiteFitnessFunction objective) {
		logger.debug("Adding branches already covered previously");

		BranchCoverageMap branchMap = BranchCoverageMap.getInstance();

		Set<Integer> uncoveredTrueBranches = new LinkedHashSet<Integer>(branchMap.getCoveredTrueBranches());
		Set<Integer> uncoveredFalseBranches = new LinkedHashSet<Integer>(branchMap.getCoveredFalseBranches());

		uncoveredTrueBranches.removeAll(getCoveredTrueBranches(individual));
		uncoveredFalseBranches.removeAll(getCoveredFalseBranches(individual));

		for (Integer branchId : uncoveredTrueBranches) {
			individual.addTest(branchMap.getTestCoveringTrue(branchId).clone());
		}
		for (Integer branchId : uncoveredFalseBranches) {
			individual.addTest(branchMap.getTestCoveringFalse(branchId).clone());
		}
	}

	/**
	 * Indicates if the fitness of the individual has improved with respected to
	 * parameter <code>fitnessBefore</code>
	 * 
	 * @param fitnessBefore
	 *            the previous fitness of the individual
	 * @param individual
	 *            the individual
	 * @param objective
	 *            the local search objective
	 * @return true if fitness improved, false otherwise
	 */
	private boolean hasImproved(double fitnessBefore, TestSuiteChromosome individual,
			LocalSearchObjective<TestSuiteChromosome> objective) {
		return objective.isMaximizationObjective() ? fitnessBefore < individual.getFitness()
				: fitnessBefore > individual.getFitness();
	}

	/**
	 * Applies local search to the suite targeting the objective passed as
	 * parameter. The type of local search will be decided according to the
	 * <code>DSE_PROBABITY</code> and <code>LOCAL_SEARCH_DSE</code> properties.
	 * 
	 * @param suite
	 *            the test suite to apply local search on
	 * 
	 * @param objective
	 *            the local search objective
	 * 
	 * @return true iff the test suite has improved.
	 */
	@Override
	public boolean doSearch(TestSuiteChromosome suite, LocalSearchObjective<TestSuiteChromosome> objective) {

		updateFitness(suite, objective.getFitnessFunctions());
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

		/*
		 * We duplicate each test case that executes a predicate only once to
		 * ensure that when measuring the coverage of the new test case produced
		 * by DSE or AVM the fitness improvement is due to an actual
		 * improvement, not because the new test case is executing a predicate
		 * that was only executed once in the original test suite.
		 */
		if (Properties.LOCAL_SEARCH_ENSURE_DOUBLE_EXECUTION) {
			ensureDoubleExecution(suite, objective);
		}

		if (Properties.LOCAL_SEARCH_RESTORE_COVERAGE) {
			restoreBranchCoverage(suite, (TestSuiteFitnessFunction) objective.getFitnessFunctions().get(0));
		}

		if (Properties.LOCAL_SEARCH_EXPAND_TESTS) {
			expandTestSuite(suite, objective);
		}

		applyLocalSearch(suite, objective);

		LocalSearchBudget.getInstance().countLocalSearchOnTestSuite();

		/*
		 * Fitness value may actually get worse if we are dealing with static
		 * state. As long as EvoSuite can't handle this, we cannot check this
		 * assertion.
		 */
		/*
		 * assert (objective.getFitnessFunction().isMaximizationFunction() ?
		 * fitnessBefore <= individual.getFitness() : fitnessBefore >=
		 * individual.getFitness()) : "Fitness was " + fitnessBefore +
		 * " and now is " + individual.getFitness();
		 */

		// Return true if fitness has improved
		boolean hasImproved = hasImproved(fitnessBefore, suite, objective);
		if (!hasImproved) {
			// restore original tests
			suite.clearTests();
			suite.addTests(originalTests);
		}
		return hasImproved;
	}

	/**
	 * This enumerate represents which type of local search will be applied on
	 * the suite
	 * 
	 * @author galeotti
	 */
	enum LocalSearchSuiteType {
		/**
		 * Always apply DSE on all test cases in the suite
		 */
		ALWAYS_DSE,
		/**
		 * Always apply AVM on all test cases in the suite
		 */
		ALWAYS_AVM,
		/**
		 * Apply AVM/DSE on a test case according to the
		 * <code>DSE_PROBABILITY</code>
		 */
		DSE_AND_AVM
	}

	/**
	 * Selects the type of local search according to the
	 * <code>LOCAL_SEARCH_DSE</code> and the <code>DSE_PROBABILITY</code>
	 * properties.
	 * 
	 * @return the type of Local Search to be applied
	 */
	private static LocalSearchSuiteType chooseLocalSearchSuiteType() {

		final LocalSearchSuiteType localSearchType;
		if (Properties.DSE_PROBABILITY <= 0.0) {
			localSearchType = LocalSearchSuiteType.ALWAYS_AVM;
		} else if (Properties.LOCAL_SEARCH_DSE == Properties.DSEType.SUITE) {
			if (Randomness.nextDouble() <= Properties.DSE_PROBABILITY) {
				localSearchType = LocalSearchSuiteType.ALWAYS_DSE;
			} else {
				localSearchType = LocalSearchSuiteType.ALWAYS_AVM;
			}
		} else {
			assert (Properties.LOCAL_SEARCH_DSE == Properties.DSEType.TEST);
			localSearchType = LocalSearchSuiteType.DSE_AND_AVM;
		}
		return localSearchType;
	}

	/**
	 * Decides the type of local search to be applied, and invokes the
	 * corresponding local search procedure.
	 * 
	 * @param suite
	 *            the suite to optimise
	 * @param objective
	 *            the local search objective
	 */
	private void applyLocalSearch(TestSuiteChromosome suite, LocalSearchObjective<TestSuiteChromosome> objective) {

		final LocalSearchSuiteType localSearchType;
		localSearchType = chooseLocalSearchSuiteType();

		/*
		 * We make a copy of the original test cases before Local Search
		 */
		List<TestChromosome> originalTests = new ArrayList<TestChromosome>(suite.getTestChromosomes());

		for (final TestChromosome test : originalTests) {

			// If we have already tried local search before on this test
			// without success, we reset all primitive values before trying
			// again
			if (test.hasLocalSearchBeenApplied()) {
				TestCaseLocalSearch.randomizePrimitives(test.getTestCase());
				updateFitness(suite, objective.getFitnessFunctions());
			}

			if (LocalSearchBudget.getInstance().isFinished()) {
				logger.debug("Local search budget used up: " + Properties.LOCAL_SEARCH_BUDGET_TYPE);
				break;
			}
			logger.debug("Local search budget not yet used up");

			final double tossCoin = Randomness.nextDouble();
			final boolean shouldApplyDSE = localSearchType == LocalSearchSuiteType.ALWAYS_DSE
					|| (localSearchType == LocalSearchSuiteType.DSE_AND_AVM && tossCoin <= Properties.DSE_PROBABILITY);

			/*
			 * We create a cloned test case to play local search with it. This
			 * resembles the deprecated ensureDoubleExecution
			 */
			TestChromosome clonedTest = (TestChromosome) test.clone();
			suite.addTest(clonedTest);
			final int lastIndex = suite.size() - 1;

			final boolean improved;
			if (shouldApplyDSE) {
				improved = applyDSE(suite, lastIndex, clonedTest, objective);
			} else {
				improved = applyAVM(suite, lastIndex, clonedTest, objective);
			}

			if (improved) {
				updateFitness(suite, objective.getFitnessFunctions());
			} else {
				// remove cloned test case if there was no improvement
				suite.deleteTest(clonedTest);
			}

			test.setLocalSearchApplied(true);
		}

	}

	/**
	 * Applies AVM on the test case in the suite
	 * 
	 * @param suite
	 * @param testIndex
	 * @param test
	 * @param localSearchObjective
	 * @return
	 */
	private boolean applyAVM(TestSuiteChromosome suite, int testIndex, TestChromosome test,
			LocalSearchObjective<TestSuiteChromosome> objective) {
		logger.debug("Local search on test " + testIndex + ", current fitness: " + suite.getFitness());
		final List<FitnessFunction<? extends Chromosome>> fitnessFunctions = objective.getFitnessFunctions();
		TestSuiteLocalSearchObjective testCaseLocalSearchObjective = TestSuiteLocalSearchObjective
				.buildNewTestSuiteLocalSearchObjective(fitnessFunctions, suite, testIndex);

		AVMTestCaseLocalSearch testCaselocalSearch = new AVMTestCaseLocalSearch();
		boolean improved = testCaselocalSearch.doSearch(test, testCaseLocalSearchObjective);
		return improved;
	}

	/**
	 * Applies DSE on the test case of the suite
	 * 
	 * @param suite
	 * @param testIndex
	 * @param test
	 * @param objective
	 * @return
	 */
	private boolean applyDSE(TestSuiteChromosome suite, int testIndex, TestChromosome test,
			LocalSearchObjective<TestSuiteChromosome> objective) {

		TestSuiteLocalSearchObjective testSuiteObject = TestSuiteLocalSearchObjective
				.buildNewTestSuiteLocalSearchObjective(objective.getFitnessFunctions(), suite, testIndex);

		DSETestCaseLocalSearch dseTestCaseLocalSearch = new DSETestCaseLocalSearch(suite);
		boolean improved = dseTestCaseLocalSearch.doSearch(test, testSuiteObject);

		return improved;
	}

}
