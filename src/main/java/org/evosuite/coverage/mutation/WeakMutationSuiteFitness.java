/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 * 
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package org.evosuite.coverage.mutation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.ExecutionResult;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;

/**
 * <p>
 * WeakMutationSuiteFitness class.
 * </p>
 * 
 * @author fraser
 */
public class WeakMutationSuiteFitness extends MutationSuiteFitness {

	private static final long serialVersionUID = -1812256816400338180L;

	/* (non-Javadoc)
	 * @see org.evosuite.ga.FitnessFunction#getFitness(org.evosuite.ga.Chromosome)
	 */
	/** {@inheritDoc} */
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
		double fitness = branchFitness.getFitness(individual);
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
		if (mostCoveredGoals < covered)
			mostCoveredGoals = covered;

		updateIndividual(individual, fitness);
		((TestSuiteChromosome) individual).setCoverage(1.0 * covered
		        / mutationGoals.size());

		return fitness;
	}
}
