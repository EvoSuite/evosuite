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
import org.evosuite.Properties.DSEType;
import org.evosuite.ga.localsearch.LocalSearchBudget;
import org.evosuite.ga.localsearch.LocalSearchObjective;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.localsearch.TestCaseLocalSearch;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.evosuite.utils.Randomness;

public class StandardTestSuiteLocalSearch extends TestSuiteLocalSearch {

	@Override
	public boolean doSearch(TestSuiteChromosome individual,
	        LocalSearchObjective<TestSuiteChromosome> objective) {
		updateFitness(individual, objective);
		double fitnessBefore = individual.getFitness();
		//logger.info("Test suite before local search: " + individual);

		List<TestChromosome> originalTests = new ArrayList<TestChromosome>(individual.getTestChromosomes());
		List<TestChromosome> tests = individual.getTestChromosomes();
		/*
		 * When we apply local search, due to budget constraints we might not be able
		 * to evaluate all the test cases in a test suite. When we apply LS several times on
		 * same individual in different generations, to avoid having always the same test cases searched for and
		 * others skipped, then we shuffle the test cases, so each time the order is different 
		 */
		Randomness.shuffle(tests);

		if(Properties.LOCAL_SEARCH_ENSURE_DOUBLE_EXECUTION)
			ensureDoubleExecution(individual, objective);

		if(Properties.LOCAL_SEARCH_RESTORE_COVERAGE)
			restoreBranchCoverage(individual, (TestSuiteFitnessFunction) objective.getFitnessFunctions().get(0));
		
		if(Properties.LOCAL_SEARCH_EXPAND_TESTS)
			expandTestSuite(individual, objective);

		if (Properties.LOCAL_SEARCH_DSE == DSEType.SUITE &&
				Randomness.nextDouble() < Properties.DSE_PROBABILITY){
			doDSESearch(individual, objective);
		} else {
			doRegularSearch(individual, objective);
		}
		LocalSearchBudget.getInstance().countLocalSearchOnTestSuite();

		// Fitness value may actually get worse if we are dealing with static state.
		// As long as EvoSuite can't handle this, we cannot check this assertion.
		//		assert (objective.getFitnessFunction().isMaximizationFunction() ? fitnessBefore <= individual.getFitness()
		//		        : fitnessBefore >= individual.getFitness()) : "Fitness was "
		//		        + fitnessBefore + " and now is " + individual.getFitness();

		// Return true if fitness has improved
		boolean hasImproved = hasImproved(fitnessBefore,  individual, objective);
		if (!hasImproved) {
			// restore original tests 
			individual.clearTests(); 
			individual.addTests(originalTests);
		}
		return hasImproved;
	}


	private void doRegularSearch(TestSuiteChromosome individual,
	        LocalSearchObjective<TestSuiteChromosome> objective) {
		List<TestChromosome> tests = individual.getTestChromosomes();
		for (int i = 0; i < tests.size(); i++) {
			TestChromosome test = tests.get(i);
			
			// If we have already tried local search before on this test
			// without success, we reset all primitive values before trying again 
			if(test.hasLocalSearchBeenApplied()) {
				TestCaseLocalSearch.randomizePrimitives(test.getTestCase());
				updateFitness(individual, objective);
			}

			logger.debug("Local search on test " + i + ", current fitness: "
			        + individual.getFitness());
			TestSuiteLocalSearchObjective testObjective = TestSuiteLocalSearchObjective.getTestSuiteLocalSearchObjective(objective.getFitnessFunctions(),
					individual, i);

			if (LocalSearchBudget.getInstance().isFinished()) {
				logger.debug("Local search budget used up: "
				        + Properties.LOCAL_SEARCH_BUDGET_TYPE);
				break;
			}
			logger.debug("Local search budget not yet used up");

			boolean improved = test.localSearch(testObjective);
			if (improved) {
				updateFitness(individual, objective);
			} else {
				//fitness cannot be worse than before by construction
				//no need to update fitness
			}
			
			test.setLocalSearchApplied(true);
		}

	}

	protected void doDSESearch(TestSuiteChromosome individual,
	        LocalSearchObjective<TestSuiteChromosome> objective) {
		TestSuiteDSE dse = new TestSuiteDSE(objective);
		dse.applyDSE(individual);
	}
}
