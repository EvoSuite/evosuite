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
package org.evosuite.testcase.localsearch;

import java.util.HashSet;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.Properties.DSEType;
import org.evosuite.ga.localsearch.LocalSearchBudget;
import org.evosuite.ga.localsearch.LocalSearchObjective;
import org.evosuite.ga.operators.mutation.MutationHistory;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestMutationHistoryEntry;
import org.evosuite.utils.Randomness;

public class SelectiveTestCaseLocalSearch extends TestCaseLocalSearch {

	@Override
	public boolean doSearch(TestChromosome individual,
			LocalSearchObjective<TestChromosome> objective) {

		logger.info("Applying local search on test case");

		boolean improved = false;

		// Only apply local search up to the point where an exception was thrown
		int lastPosition = individual.size() - 1;
		if (individual.getLastExecutionResult() != null && !individual.isChanged()) {
			Integer lastPos = individual.getLastExecutionResult().getFirstPositionOfThrownException();
			if (lastPos != null)
				lastPosition = lastPos.intValue();
		}

		Set<Integer> targetPositions = new HashSet<Integer>();

		logger.info("Mutation history: " + individual.getMutationHistory().toString());
		logger.info("Checking {} mutations", individual.getMutationHistory().size());
		MutationHistory<TestMutationHistoryEntry> history = new MutationHistory<>();
		history.set(individual.getMutationHistory());

        boolean useDSE = Properties.LOCAL_SEARCH_DSE == DSEType.TEST &&
                Randomness.nextDouble() < Properties.DSE_PROBABILITY;

        for (TestMutationHistoryEntry mutation : individual.getMutationHistory()) {
			if (LocalSearchBudget.getInstance().isFinished())
				break;

			// Reference local search may have removed the statement
			if(!individual.getTestCase().contains(mutation.getStatement()))
				continue;
			
			if (mutation.getMutationType() != TestMutationHistoryEntry.TestMutation.DELETION
			        && mutation.getStatement().getPosition() <= lastPosition) {
//			        && mutation.getStatement() instanceof PrimitiveStatement<?>) {
				logger.info("Found suitable mutation: " + mutation);

				final Class<?> targetClass = Properties.getTargetClassAndDontInitialise();

				if (!individual.getTestCase().hasReferences(mutation.getStatement().getReturnValue())
				        && !mutation.getStatement().getReturnClass().equals(targetClass)) {
					logger.info("Return value of statement "
					        + " is not referenced and not SUT, not doing local search");
					continue;
				}

				if(useDSE) {
                    targetPositions.add(mutation.getStatement().getPosition());
                } else {
					StatementLocalSearch search = StatementLocalSearch.getLocalSearchFor(mutation.getStatement());
					if (search != null) {
						if (search.doSearch(individual, mutation.getStatement().getPosition(), (LocalSearchObjective<TestChromosome>) objective)) {
							improved = true;
						}
						// i += search.getPositionDelta();
					}
				}

			} else {
				logger.info("Unsuitable mutation");
			}
		}

		if (!targetPositions.isEmpty()) {
			logger.info("Yes, now applying the search at positions {}!", targetPositions);
			DSELocalSearch dse = new DSELocalSearch();
			assert improved==false;
			improved = dse.doSearch(individual, targetPositions,
			             (LocalSearchObjective<TestChromosome>) objective);
		}
		individual.getMutationHistory().clear();

		LocalSearchBudget.getInstance().countLocalSearchOnTest();

		//logger.info("Test after local search: " + test.toCode());

		// Return true iif search was successful
		return improved;

		// TODO: Handle arrays in local search
		// TODO: mutating an int might have an effect on array lengths
		
	}

}
