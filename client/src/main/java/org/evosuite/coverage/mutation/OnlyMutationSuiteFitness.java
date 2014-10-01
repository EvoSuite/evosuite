package org.evosuite.coverage.mutation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.ExecutionResult;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;

public class OnlyMutationSuiteFitness extends MutationSuiteFitness {

	private static final long serialVersionUID = 7645950782176885889L;

	@Override
	public double getFitness(
	        AbstractTestSuiteChromosome<? extends ExecutableChromosome> individual) {
		List<ExecutionResult> results = runTestSuite(individual);

		// First objective: achieve branch coverage
		logger.debug("Calculating branch fitness: ");
		/*
		 * Note: results are cached, so the test suite is not executed again when we
		 * calculated the branch fitness
		 */
		double fitness = 0.0;
		Map<Integer, Double> mutant_distance = new HashMap<Integer, Double>();
		Set<Integer> touchedMutants = new HashSet<Integer>();

		for (ExecutionResult result : results) {
			touchedMutants.addAll(result.getTrace().getTouchedMutants());

			for (Entry<Integer, Double> entry : result.getTrace().getMutationDistances().entrySet()) {
				if (!mutant_distance.containsKey(entry.getKey()))
					mutant_distance.put(entry.getKey(), entry.getValue());
				else {
					mutant_distance.put(entry.getKey(),
					                    Math.min(mutant_distance.get(entry.getKey()),
					                             entry.getValue()));
				}
			}
		}

		// Second objective: touch all mutants?
		fitness += MutationPool.getMutantCounter() - touchedMutants.size();
		int covered = 0;

		for (Double distance : mutant_distance.values()) {
			if (distance < 0) {
				logger.warn("Distance is " + distance + " / " + Integer.MAX_VALUE + " / "
				        + Integer.MIN_VALUE);
				distance = 0.0; // FIXXME
			}

			fitness += normalize(distance);
			if (distance == 0.0)
				covered++;
		}
		
		if (mutationGoals.size() == 0)
			assert(covered == 0);

		updateIndividual(this, individual, fitness);
		((TestSuiteChromosome) individual).setCoverage(this, mutationGoals.size() == 0.0 ? 0.0 :
			1.0 * covered / mutationGoals.size());
		((TestSuiteChromosome) individual).setNumOfCoveredGoals(this, covered);
		
		return fitness;
	}
}
