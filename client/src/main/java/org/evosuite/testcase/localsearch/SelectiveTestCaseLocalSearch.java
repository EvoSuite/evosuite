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
import org.evosuite.testcase.statements.PrimitiveStatement;
import org.evosuite.utils.Randomness;

public class SelectiveTestCaseLocalSearch extends TestCaseLocalSearch {

	@Override
	public boolean doSearch(TestChromosome individual,
			LocalSearchObjective<TestChromosome> objective) {

		double oldFitness = individual.getFitness();
		logger.info("Applying local search on test case");

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

				if (!individual.getTestCase().hasReferences(mutation.getStatement().getReturnValue())
				        && !mutation.getStatement().getReturnClass().equals(Properties.getTargetClass())) {
					logger.info("Return value of statement "
					        + " is not referenced and not SUT, not doing local search");
					continue;
				}

				if(useDSE) {
                    targetPositions.add(mutation.getStatement().getPosition());
                } else {
					StatementLocalSearch search = StatementLocalSearch.getLocalSearchFor(mutation.getStatement());
					if (search != null) {
						search.doSearch(individual, mutation.getStatement().getPosition(), (LocalSearchObjective<TestChromosome>) objective);
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
			boolean dseWasSuccessfull = dse.doSearch(individual, targetPositions,
			             (LocalSearchObjective<TestChromosome>) objective);
		}
		individual.getMutationHistory().clear();

		LocalSearchBudget.getInstance().countLocalSearchOnTest();

		assert individual.getFitness() <= oldFitness;
		// Return true if fitness has improved
		return objective.getFitnessFunction().isMaximizationFunction() ? oldFitness < individual.getFitness(): oldFitness > individual.getFitness();

		//logger.info("Test after local search: " + test.toCode());

		// TODO: Handle arrays in local search
		// TODO: mutating an int might have an effect on array lengths
		
	}

}
