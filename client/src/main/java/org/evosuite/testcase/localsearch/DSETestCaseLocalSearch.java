package org.evosuite.testcase.localsearch;

import java.util.HashSet;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.ga.localsearch.LocalSearchBudget;
import org.evosuite.ga.localsearch.LocalSearchObjective;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;

public class DSETestCaseLocalSearch extends TestCaseLocalSearch {

	private final TestSuiteChromosome suite;

	/**
	 * Creates a DSE local search with no whole test suite
	 */
	public DSETestCaseLocalSearch() {
		this(null);
	}

	/**
	 * Creates a DSE local search for a test case within a specific whole test
	 * suite
	 * 
	 * @param suite
	 */
	public DSETestCaseLocalSearch(TestSuiteChromosome suite) {
		this.suite = suite;
	}

	/**
	 * Applies DSE local search on a test case using the passed local search
	 * objective. The local search modifies the test chromosome, it cannot
	 * create a fresh new test chromosome.
	 * 
	 * @param testChromosome
	 *            the test case chromosome
	 * @param localSearchObjective
	 *            the fitness functions for the test chromosome
	 */
	@Override
	public boolean doSearch(TestChromosome testChromosome, LocalSearchObjective<TestChromosome> localSearchObjective) {

		logger.info("Test before local search: " + testChromosome.getTestCase().toCode());

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

		boolean improved = false;
		if (!targetStatementIndexes.isEmpty()) {
			logger.info("Yes, now applying the search at positions {}!", targetStatementIndexes);
			DSEStatementLocalSearch dseStatementLocalSearch;
			if (suite != null) {
				dseStatementLocalSearch = new DSEStatementLocalSearch(suite);
			} else {
				dseStatementLocalSearch = new DSEStatementLocalSearch();
			}
			improved = dseStatementLocalSearch.doSearch(testChromosome, targetStatementIndexes, localSearchObjective);
		}

		LocalSearchBudget.getInstance().countLocalSearchOnTest();

		// logger.warn("Test after local search: " +
		// individual.getTestCase().toCode());

		// Return true iif search was successful
		return improved;

		// TODO: Handle arrays in local search
		// TODO: mutating an int might have an effect on array lengths
	}

}
