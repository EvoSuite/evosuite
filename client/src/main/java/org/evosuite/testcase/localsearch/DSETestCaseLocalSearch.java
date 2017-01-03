package org.evosuite.testcase.localsearch;

import java.util.HashSet;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.ga.localsearch.LocalSearchBudget;
import org.evosuite.ga.localsearch.LocalSearchObjective;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionTrace;
import org.evosuite.testsuite.TestSuiteChromosome;

/**
 * Applies DSE on a given test case. If the test case belongs to a suite, it
 * should be provided by using the constructor that receives a suite chromosome.
 * 
 * If the test case has no symbolic variables (or these variables are not
 * reached due to a thrown exception during test execution), or it does not
 * reach an uncovered branch, DSE is skipped on this test case.
 * 
 * @author galeotti
 */
public class DSETestCaseLocalSearch extends TestCaseLocalSearch {

	/**
	 * Returns true iff the test reaches a decision (if/while) with an uncovered
	 * branch
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

	private final TestSuiteChromosome suite;

	/**
	 * Creates a DSE local search with no whole test suite
	 */
	public DSETestCaseLocalSearch() {
		this(null);
	}

	/**
	 * Creates a DSE local search for a test case that belongs to a whole test
	 * suite
	 * 
	 * @param suite
	 */
	public DSETestCaseLocalSearch(TestSuiteChromosome suite) {
		this.suite = suite;
	}

	/**
	 * Returns those branches that are reached but are not covered
	 * 
	 * @param coveredBranches
	 * @return
	 */
	private static Set<Branch> collectUncoveredBranches(Set<Branch> coveredBranches) {
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
	 * Applies DSE on a test case using the passed local search objective. The
	 * local search <b>modifies</b> the test chromosome, it cannot create a
	 * fresh new test chromosome. If the test case has no symbolic variables, or
	 * it does not reach an uncovered branch, DSE is skipped.
	 * 
	 * @param test
	 *            the test case chromosome.
	 * 
	 * @param objective
	 *            the fitness functions for the test chromosome
	 */
	@Override
	public boolean doSearch(TestChromosome test, LocalSearchObjective<TestChromosome> objective) {
		logger.info("Test before local search: " + test.getTestCase().toCode());

		// gather covered branches true/false branch indexes

		final Set<Branch> coveredBranches;
		if (suite != null) {
			coveredBranches = collectCoveredBranches(suite);
		} else {
			coveredBranches = getCoveredBranches(test);
		}
		final Set<Branch> uncoveredBranches = collectUncoveredBranches(coveredBranches);

		if (uncoveredBranches.isEmpty()) {
			/*
			 * As there are no branches uncovered (true or false branch missing)
			 * in this suite, there is no point in continuing DSE. Therefore, we
			 * should stop DSE (no DSE improvement)
			 */
			return false;
		}
 
		if (!hasUncoveredBranch(test, uncoveredBranches)) {
			/*
			 * As there are uncovered branches, but none is reached by this
			 * test, the DSE is skipped and we return false (no DSE improvement)
			 */
			return false;
		}

		Set<Integer> targetStatementIndexes = collectStatementIndexesWithSymbolicVariables(test, objective);

		final boolean fitnessHasImproved;
		if (targetStatementIndexes.isEmpty()) {
			// Cannot apply DSE because there are no symbolic variables
			// Therefore, no improvement on objective.
			fitnessHasImproved = false;
		} else {
			logger.info("Yes, now applying the search at positions {}!", targetStatementIndexes);
			DSETestGenerator dseTestGenerator;
			if (suite != null) {
				dseTestGenerator = new DSETestGenerator(suite);
			} else {
				dseTestGenerator = new DSETestGenerator();
			}
			final TestChromosome newTest = dseTestGenerator.generateNewTest(test, targetStatementIndexes, objective);
			if (newTest != null) {
				fitnessHasImproved = true;
			} else {
				fitnessHasImproved = false;
			}
		}

		LocalSearchBudget.getInstance().countLocalSearchOnTest();

		// Return true iff search was successful
		return fitnessHasImproved;

		// TODO: Handle arrays in local search
		// TODO: mutating an int might have an effect on array lengths
	}

	/**
	 * Collects a set with the indexes of those positions that have symbolic
	 * variables. If the symbolic variable is beyond a statement that throws an
	 * exception, it is ignored.
	 * 
	 * @param testChromosome
	 *            the test case to apply DSE
	 * @param localSearchObjective
	 *            the objective to measure fitness improvement
	 * @return a set with statement indexes in the test case with symbolic
	 *         variables
	 */
	private static Set<Integer> collectStatementIndexesWithSymbolicVariables(TestChromosome testChromosome,
			LocalSearchObjective<TestChromosome> localSearchObjective) {
		// Only apply local search up to the point where an exception was thrown
		// TODO: Check whether this conflicts with test expansion
		int lastPosition = testChromosome.size() - 1;
		if (testChromosome.getLastExecutionResult() != null && !testChromosome.isChanged()) {
			Integer lastPos = testChromosome.getLastExecutionResult().getFirstPositionOfThrownException();
			if (lastPos != null)
				lastPosition = lastPos.intValue();
		}
		TestCase test = testChromosome.getTestCase();
		Set<Integer> targetStatementIndexes = new HashSet<Integer>();

		// We count down to make the code work when lines are
		// added during the search (see NullReferenceSearch).

		for (int i = lastPosition; i >= 0; i--) {
			if (LocalSearchBudget.getInstance().isFinished())
				break;

			if (localSearchObjective.isDone()) {
				break;
			}

			if (i >= testChromosome.size()) {
				logger.warn("Test size decreased unexpectedly during local search, aborting local search");
				logger.warn(testChromosome.getTestCase().toCode());
				break;
			}
			final Class<?> targetClass = Properties.getTargetClassAndDontInitialise();

			if (!test.hasReferences(test.getStatement(i).getReturnValue())
					&& !test.getStatement(i).getReturnClass().equals(targetClass)) {
				logger.info(
						"Return value of statement " + i + " is not referenced and not SUT, not doing local search");
				continue;
			}

			targetStatementIndexes.add(i);

		}
		return targetStatementIndexes;
	}

	/**
	 * Returns the set covered branches by this suite
	 * 
	 * @param suite
	 * @return
	 */
	private static Set<Branch> collectCoveredBranches(TestSuiteChromosome suite) {
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

}
