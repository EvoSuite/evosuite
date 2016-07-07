package org.evosuite.testsuite.localsearch;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.ga.localsearch.LocalSearchBudget;
import org.evosuite.ga.localsearch.LocalSearchObjective;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionTrace;
import org.evosuite.testcase.localsearch.DSETestCaseLocalSearch;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DSETestSuiteLocalSearch extends TestSuiteLocalSearch {

	/**
	 * Applies local search to a test suite using a local search objective to
	 * measure the fitness improvement.
	 */
	@Override
	public boolean doSearch(TestSuiteChromosome suite, LocalSearchObjective<TestSuiteChromosome> localSearchObjective) {
		this.objective = localSearchObjective;

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

		// if (Properties.LOCAL_SEARCH_ENSURE_DOUBLE_EXECUTION)
		// ensureDoubleExecution(suite, localSearchObjective);

		if (Properties.LOCAL_SEARCH_RESTORE_COVERAGE)
			restoreBranchCoverage(suite, (TestSuiteFitnessFunction) localSearchObjective.getFitnessFunctions().get(0));

		if (Properties.LOCAL_SEARCH_EXPAND_TESTS)
			expandTestSuite(suite, localSearchObjective);

		this.applyDSE(suite);

		LocalSearchBudget.getInstance().countLocalSearchOnTestSuite();

		// Return true if fitness has improved
		boolean hasImproved = hasImproved(fitnessBefore, suite, localSearchObjective);
		if (!hasImproved) {
			// restore original tests
			suite.clearTests();
			suite.addTests(originalTests);
		}
		return hasImproved;

	}

	private static final Logger logger = LoggerFactory.getLogger(DSETestSuiteLocalSearch.class);

	private LocalSearchObjective<TestSuiteChromosome> objective;

	/**
	 * Represents a branch in the target program
	 * 
	 * @author galeotti
	 */
	private static class Branch {

		public Branch(int branchIndex, boolean isTrueBranch) {
			super();
			this.branchIndex = branchIndex;
			this.isTrueBranch = isTrueBranch;
		}

		private int branchIndex;

		private boolean isTrueBranch;

		public Branch negate() {
			return new Branch(branchIndex, !isTrueBranch);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + branchIndex;
			result = prime * result + (isTrueBranch ? 1231 : 1237);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Branch other = (Branch) obj;
			if (branchIndex != other.branchIndex)
				return false;
			if (isTrueBranch != other.isTrueBranch)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "Branch [branchIndex=" + branchIndex + ", isTrueBranch=" + isTrueBranch + "]";
		}

	}

	// private final TestSuiteFitnessFunction fitness;

	/**
	 * Attempt to negate individual branches until budget is used up, or there
	 * are no further branches to negate
	 * 
	 * @param individual
	 */
	private boolean applyDSE(TestSuiteChromosome suite) {
		updateFitness(suite, this.objective.getFitnessFunctions());

		double fitnessBefore = suite.getFitness();
		// logger.info("Test suite before local search: " + individual);

		List<TestChromosome> tests = suite.getTestChromosomes();
		/*
		 * When we apply local search, due to budget constraints we might not be
		 * able to evaluate all the test cases in a test suite. When we apply LS
		 * several times on same individual in different generations, to avoid
		 * having always the same test cases searched for and others skipped,
		 * then we shuffle the test cases, so each time the order is different
		 */
		Randomness.shuffle(tests);

		boolean testSuiteImproved = false;

		for (int testIndex = 0; testIndex < tests.size(); testIndex++) {

			// gather covered branches true/false branch indexes
			final Set<Branch> suiteCoveredBranches = getCoveredBranches(suite);
			final Set<Branch> uncoveredBranches = getUncoveredBranches(suiteCoveredBranches);

			if (uncoveredBranches.isEmpty()) {
				// as the suite does not reach any uncovered true or false
				// branch
				// we should stop DSE
				return testSuiteImproved;
			}

			if (LocalSearchBudget.getInstance().isFinished()) {
				logger.debug("Local search budget used up: " + Properties.LOCAL_SEARCH_BUDGET_TYPE);
				break;
			}

			final TestChromosome test = tests.get(testIndex);

			final boolean reachesUncoveredBranch = hasUncoveredBranch(test, uncoveredBranches);
			if (!reachesUncoveredBranch) {
				logger.debug("Skipping test case since it does not reach any uncovered branch");
				continue;
			}

			boolean improved = applyDSE(suite, test);

			if (improved) {
				testSuiteImproved = true;
				testIndex--;
			}
		}

		if (testSuiteImproved) {
			if (!this.hasImproved(fitnessBefore, suite, this.objective)) {
				logger.warn("DSE reported improved but fitness has not changed!");
			}
		}

		return testSuiteImproved;
	}

	/**
	 * Returns true iff the test reaches an uncovered branch
	 * 
	 * @param test
	 * @param uncoveredBranches
	 * @return
	 */
	private static boolean hasUncoveredBranch(TestChromosome test, Set<Branch> uncoveredBranches) {
		Set<Branch> testCoveredBranches = getCoveredBranches(test);
		for (Branch b : testCoveredBranches) {
			Branch negate = b.negate();
			if (uncoveredBranches.contains(negate)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns those branches that are reached but are not covered
	 * 
	 * @param coveredBranches
	 * @return
	 */
	private static Set<Branch> getUncoveredBranches(Set<Branch> coveredBranches) {
		Set<Branch> uncoveredBranches = new HashSet<Branch>();
		for (Branch b : coveredBranches) {
			final Branch negate = b.negate();
			if (!coveredBranches.contains(negate)) {
				uncoveredBranches.add(negate);
			}
		}
		return uncoveredBranches;
	}

	/**
	 * Returns the set covered branches by this suite
	 * 
	 * @param suite
	 * @return
	 */
	private static Set<Branch> getCoveredBranches(TestSuiteChromosome suite) {
		final Set<Branch> suiteCoveredBranches = new HashSet<Branch>();
		for (TestChromosome test : suite.getTestChromosomes()) {
			final Set<Branch> testCoveredBranches = getCoveredBranches(test);
			suiteCoveredBranches.addAll(testCoveredBranches);
		}
		return suiteCoveredBranches;

	}

	/**
	 * Returns the set of covered branches by this test
	 * 
	 * @param test
	 * @return
	 */
	private static Set<Branch> getCoveredBranches(TestChromosome test) {
		final Set<Branch> testCoveredBranches = new HashSet<Branch>();

		ExecutionTrace trace = test.getLastExecutionResult().getTrace();
		{
			Set<Integer> coveredTrueBranchIndexesInTrace = trace.getCoveredTrueBranches();
			for (Integer branchIndex : coveredTrueBranchIndexesInTrace) {
				Branch b = new Branch(branchIndex, true);
				testCoveredBranches.add(b);
			}
		}
		{

			Set<Integer> coveredFalseBranchIndexesInTrace = trace.getCoveredFalseBranches();
			for (Integer branchIndex : coveredFalseBranchIndexesInTrace) {
				Branch b = new Branch(branchIndex, false);
				testCoveredBranches.add(b);
			}
		}
		return testCoveredBranches;
	}

	/**
	 * Apply DSE on an specific test within the whole test suite. The test must
	 * be contained in the whole test suite
	 *
	 * The test suite can have a new test case if the fitness was improved due
	 * to DSE.
	 * 
	 * @param suite
	 * @param test
	 * @return
	 */
	private boolean applyDSE(TestSuiteChromosome suite, TestChromosome test) {

		// add a placeholder for the DSE new test
		TestChromosome clonedTest = (TestChromosome) test.clone();
		suite.addTest(clonedTest);
		final int lastIndex = suite.size() - 1;

		TestSuiteLocalSearchObjective testSuiteObject = TestSuiteLocalSearchObjective
				.buildNewTestSuiteLocalSearchObjective(this.objective.getFitnessFunctions(), suite, lastIndex);

		DSETestCaseLocalSearch dseTestCaseLocalSearch = new DSETestCaseLocalSearch(suite);
		boolean improved = dseTestCaseLocalSearch.doSearch(clonedTest, testSuiteObject);

		if (!improved) {
			// remove added test case
			suite.deleteTest(clonedTest);
		}

		return improved;
	}

}
