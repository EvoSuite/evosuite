package org.evosuite.testsuite.localsearch;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.ga.localsearch.LocalSearchBudget;
import org.evosuite.ga.localsearch.LocalSearchObjective;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.evosuite.utils.Randomness;

public class DSETestSuiteLocalSearch extends TestSuiteLocalSearch {

	@Override
	public boolean doSearch(TestSuiteChromosome individual, LocalSearchObjective<TestSuiteChromosome> objective) {

		updateFitness(individual, objective);
		double fitnessBefore = individual.getFitness();
		// logger.info("Test suite before local search: " + individual);

		List<TestChromosome> originalTests = new ArrayList<TestChromosome>(individual.getTestChromosomes());
		List<TestChromosome> tests = individual.getTestChromosomes();
		/*
		 * When we apply local search, due to budget constraints we might not be
		 * able to evaluate all the test cases in a test suite. When we apply LS
		 * several times on same individual in different generations, to avoid
		 * having always the same test cases searched for and others skipped,
		 * then we shuffle the test cases, so each time the order is different
		 */
		Randomness.shuffle(tests);

		if (Properties.LOCAL_SEARCH_ENSURE_DOUBLE_EXECUTION)
			ensureDoubleExecution(individual, objective);

		if (Properties.LOCAL_SEARCH_RESTORE_COVERAGE)
			restoreBranchCoverage(individual, (TestSuiteFitnessFunction) objective.getFitnessFunctions().get(0));

		if (Properties.LOCAL_SEARCH_EXPAND_TESTS)
			expandTestSuite(individual, objective);

		TestSuiteDSE dse = new TestSuiteDSE(objective);
		dse.applyDSE(individual);

		LocalSearchBudget.getInstance().countLocalSearchOnTestSuite();

		// Return true if fitness has improved
		boolean hasImproved = hasImproved(fitnessBefore, individual, objective);
		if (!hasImproved) {
			// restore original tests
			individual.clearTests();
			individual.addTests(originalTests);
		}
		return hasImproved;

	}
}
