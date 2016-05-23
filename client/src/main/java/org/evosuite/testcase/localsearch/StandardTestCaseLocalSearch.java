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
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.utils.Randomness;

public class StandardTestCaseLocalSearch extends TestCaseLocalSearch {

	@Override
	public boolean doSearch(TestChromosome individual,
			LocalSearchObjective<TestChromosome> objective) {

		logger.info("Test before local search: " + individual.getTestCase().toCode());

		boolean improved = false;
		
		// Only apply local search up to the point where an exception was thrown
		// TODO: Check whether this conflicts with test expansion
		int lastPosition = individual.size() - 1;
		if (individual.getLastExecutionResult() != null && !individual.isChanged()) {
			Integer lastPos = individual.getLastExecutionResult().getFirstPositionOfThrownException();
			if (lastPos != null)
				lastPosition = lastPos.intValue();
		}
		TestCase test = individual.getTestCase();
		Set<Integer> targetPositions = new HashSet<Integer>();

		//We count down to make the code work when lines are
		//added during the search (see NullReferenceSearch).

        boolean useDSE = Properties.LOCAL_SEARCH_DSE == DSEType.TEST &&
                Randomness.nextDouble() < Properties.DSE_PROBABILITY;

		for (int i = lastPosition; i >= 0; i--) {
			if (LocalSearchBudget.getInstance().isFinished())
				break;

			if(objective.isDone()) {
				break;
			}
			
			if (i >= individual.size()) {
				logger.warn("Test size decreased unexpectedly during local search, aborting local search");
				logger.warn(individual.getTestCase().toCode());
				break;
			}
			final Class<?> targetClass = Properties.getTargetClassAndDontInitialise();

			if (!test.hasReferences(test.getStatement(i).getReturnValue())
					&& !test.getStatement(i).getReturnClass().equals(targetClass)) {
				logger.info("Return value of statement " + i
						+ " is not referenced and not SUT, not doing local search");
				continue;
			}

			if(useDSE)
				targetPositions.add(i);
			else {
				StatementLocalSearch search = StatementLocalSearch.getLocalSearchFor(test.getStatement(i));
				
				if (search != null) {
					logger.info("Applying local search of type "+search.getClass()+" to statement "+test.getStatement(i) +" / "+individual.getTestCase().getStatement(i));
					if(search.doSearch(individual, i, (LocalSearchObjective<TestChromosome>) objective)) {
						improved = true;
					}
					// i = s.getPosition();
					logger.debug("Old position was: "+i+", adjusting to: "+ (i + search.getPositionDelta()));
					i += search.getPositionDelta();
					test = individual.getTestCase();
				}
			}
		}
		if (!targetPositions.isEmpty()) {
			logger.info("Yes, now applying the search at positions {}!", targetPositions);
			DSELocalSearch dse = new DSELocalSearch();
			assert improved==false;
			improved = dse.doSearch(individual, targetPositions,
			             (LocalSearchObjective<TestChromosome>) objective);
		}

		LocalSearchBudget.getInstance().countLocalSearchOnTest();

		// logger.warn("Test after local search: " + individual.getTestCase().toCode());

		// Return true iif search was successful
		return improved;
		
		// TODO: Handle arrays in local search
		// TODO: mutating an int might have an effect on array lengths
	}

}
