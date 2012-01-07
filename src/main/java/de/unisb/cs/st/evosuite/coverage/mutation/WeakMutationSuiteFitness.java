/**
 * 
 */
package de.unisb.cs.st.evosuite.coverage.mutation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteChromosome;

/**
 * @author fraser
 * 
 */
public class WeakMutationSuiteFitness extends MutationSuiteFitness {

	private static final long serialVersionUID = -1812256816400338180L;

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.FitnessFunction#getFitness(de.unisb.cs.st.evosuite.ga.Chromosome)
	 */
	@Override
	public double getFitness(Chromosome individual) {
		List<ExecutionResult> results = runTestSuite((TestSuiteChromosome) individual);

		// First objective: achieve branch coverage
		logger.debug("Calculating branch fitness: ");
		double fitness = branchFitness.getFitness(individual);
		Map<Integer, Double> mutant_distance = new HashMap<Integer, Double>();
		Set<Integer> touchedMutants = new HashSet<Integer>();

		for (ExecutionResult result : results) {
			touchedMutants.addAll(result.getTrace().touchedMutants);

			for (Entry<Integer, Double> entry : result.getTrace().mutant_distances.entrySet()) {
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
			}

			fitness += normalize(distance);
			if (distance == 0.0)
				covered++;
		}
		if (mostCoveredGoals < covered)
			mostCoveredGoals = covered;

		updateIndividual(individual, fitness);
		((TestSuiteChromosome) individual).setCoverage(1.0 * covered
		        / mutationGoals.size());

		return fitness;
	}
}
