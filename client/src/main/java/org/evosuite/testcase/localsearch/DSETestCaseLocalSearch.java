package org.evosuite.testcase.localsearch;

import java.util.HashSet;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.ga.localsearch.LocalSearchBudget;
import org.evosuite.ga.localsearch.LocalSearchObjective;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;

public class DSETestCaseLocalSearch extends TestCaseLocalSearch {

	@Override
	public boolean doSearch(TestChromosome individual,
			LocalSearchObjective<TestChromosome> objective) {

        
		logger.info("Test before local search: " + individual.getTestCase().toCode());
		
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

			targetPositions.add(i);

		}
		
		boolean improved = false;
		if (!targetPositions.isEmpty()) {
			logger.info("Yes, now applying the search at positions {}!", targetPositions);
			DSEStatementLocalSearch dse = new DSEStatementLocalSearch();
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
