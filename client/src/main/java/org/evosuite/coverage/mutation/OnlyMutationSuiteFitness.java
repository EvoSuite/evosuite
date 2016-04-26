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
package org.evosuite.coverage.mutation;

import org.evosuite.Properties;
import org.evosuite.coverage.archive.TestsArchive;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;

import java.util.*;
import java.util.Map.Entry;

/**
 * <p>
 * OnlyMutationSuiteFitness class.
 * </p>
 *
 * @author fraser
 */
public class OnlyMutationSuiteFitness extends MutationSuiteFitness {

	private static final long serialVersionUID = -8194940669364526758L;

	public final Set<Integer> mutants = new HashSet<Integer>();

	public final Set<Integer> removedMutants = new HashSet<Integer>();

	public final Set<Integer> toRemoveMutants = new HashSet<Integer>();

	public final Map<Integer, MutationTestFitness> mutantMap = new HashMap<Integer, MutationTestFitness>();
	
	public OnlyMutationSuiteFitness() {
		for(MutationTestFitness goal : mutationGoals) {
			mutantMap.put(goal.getMutation().getId(), goal);
			mutants.add(goal.getMutation().getId());
			if(Properties.TEST_ARCHIVE)
				TestsArchive.instance.addGoalToCover(this, goal);
		}
	}
	
	@Override
	public boolean updateCoveredGoals() {
		if(!Properties.TEST_ARCHIVE)
			return false;
		
		for (Integer mutant : toRemoveMutants) {
			boolean removed = mutants.remove(mutant);
			TestFitnessFunction f = mutantMap.remove(mutant);
			if (removed && f != null) {
				removedMutants.add(mutant);
			} else {
				throw new IllegalStateException("goal to remove not found");
			}
		}

		toRemoveMutants.clear();
		logger.info("Current state of archive: "+TestsArchive.instance.toString());
		
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.evosuite.ga.FitnessFunction#getFitness(org.evosuite.ga.Chromosome)
	 */
	/** {@inheritDoc} */
	@Override
	public double getFitness(
	        AbstractTestSuiteChromosome<? extends ExecutableChromosome> individual) {
		/**
		 * e.g. classes with only static constructors
		 */
		if (mutationGoals.size() == 0) {
			updateIndividual(this, individual, 0.0);
			((TestSuiteChromosome) individual).setCoverage(this, 1.0);
			((TestSuiteChromosome) individual).setNumOfCoveredGoals(this, 0);
			return 0.0;
		}

		List<ExecutionResult> results = runTestSuite(individual);

		/*
		 * Note: results are cached, so the test suite is not executed again when we
		 * calculated the branch fitness
		 */
		double fitness = 0.0;
		Map<Integer, Double> mutant_distance = new HashMap<Integer, Double>();
		Set<Integer> touchedMutants = new HashSet<Integer>(removedMutants);

		for (ExecutionResult result : results) {
			// Using private reflection can lead to false positives
			// that represent unrealistic behaviour. Thus, we only
			// use reflection for basic criteria, not for mutation
			if(result.calledReflection())
				continue;

			touchedMutants.addAll(result.getTrace().getTouchedMutants());

			for (Entry<Integer, Double> entry : result.getTrace().getMutationDistances().entrySet()) {
				if(!mutants.contains(entry.getKey()) || removedMutants.contains(entry.getKey()))
					continue;
				if(entry.getValue() == 0.0) {
					result.test.addCoveredGoal(mutantMap.get(entry.getKey()));
					if(Properties.TEST_ARCHIVE) {
						toRemoveMutants.add(entry.getKey());
						TestsArchive.instance.putTest(this, mutantMap.get(entry.getKey()), result);
						individual.isToBeUpdated(true);
					}
				}
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
		int covered = removedMutants.size();

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
		
		updateIndividual(this, individual, fitness);
		((TestSuiteChromosome) individual).setCoverage(this, 1.0 * covered / mutationGoals.size());
		((TestSuiteChromosome) individual).setNumOfCoveredGoals(this, covered);
		
		return fitness;
	}
}
