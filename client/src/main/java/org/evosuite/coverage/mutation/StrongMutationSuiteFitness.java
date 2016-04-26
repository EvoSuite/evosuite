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
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.ExecutionTrace;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;

import java.util.*;

/**
 * <p>
 * StrongMutationSuiteFitness class.
 * </p>
 *
 * @author fraser
 */
public class StrongMutationSuiteFitness extends MutationSuiteFitness {

	private static final long serialVersionUID = -9124328839917834720L;

	/** {@inheritDoc} */
	@Override
	public ExecutionResult runTest(TestCase test) {
		return runTest(test, null);
	}

	/** {@inheritDoc} */
	@Override
	public ExecutionResult runTest(TestCase test, Mutation mutant) {

		return StrongMutationTestFitness.runTest(test, mutant);
	}

	/**
	 * Create a list of test cases ordered by their execution time. The
	 * precondition is that all TestChromomes have been executed such that they
	 * have an ExecutionResult.
	 * 
	 * @param individual
	 * @return
	 */
	private List<TestChromosome> prioritizeTests(TestSuiteChromosome individual) {
		List<TestChromosome> executionOrder = new ArrayList<TestChromosome>(
		        individual.getTestChromosomes());

		Collections.sort(executionOrder, new Comparator<TestChromosome>() {

			@Override
			public int compare(TestChromosome tc1, TestChromosome tc2) {
				ExecutionResult result1 = tc1.getLastExecutionResult();
				ExecutionResult result2 = tc2.getLastExecutionResult();
				long diff = result1.getExecutionTime() - result2.getExecutionTime();
				if (diff == 0)
					return 0;
				else if (diff < 0)
					return -1;
				else
					return 1;
			}

		});

		return executionOrder;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.FitnessFunction#getFitness(org.evosuite.ga.Chromosome)
	 */
	/** {@inheritDoc} */
	@Override
	public double getFitness(
	        AbstractTestSuiteChromosome<? extends ExecutableChromosome> individual) {
		runTestSuite(individual);

		// Set<MutationTestFitness> uncoveredMutants = MutationTestPool.getUncoveredFitnessFunctions();
		TestSuiteChromosome suite = (TestSuiteChromosome) individual;

		for (TestChromosome test : suite.getTestChromosomes()) {
			ExecutionResult result = test.getLastExecutionResult();

			if (result.hasTimeout()) {
				logger.debug("Skipping test with timeout");
				double fitness = branchFitness.totalBranches * 2
				        + branchFitness.totalMethods + 3 * mutationGoals.size();
				updateIndividual(this, individual, fitness);
				suite.setCoverage(this, 0.0);
				logger.info("Test case has timed out, setting fitness to max value "
				        + fitness);
				return fitness;
			}
		}

		// First objective: achieve branch coverage
		logger.debug("Calculating branch fitness: ");
		double fitness = branchFitness.getFitness(individual);

		Set<Integer> touchedMutants = new HashSet<Integer>();		
		Map<Mutation, Double> minMutantFitness = new HashMap<Mutation, Double>();

		// For each mutant that is not in the archive:
		//   3    -> not covered
		//   1..2 -> infection distance
		//   0..1 -> propagation distance
		for (Integer mutantId : mutants) {
			MutationTestFitness mutantFitness = mutantMap.get(mutantId);
			minMutantFitness.put(mutantFitness.getMutation(), 3.0);
		}
		
		int mutantsChecked = 0;
		int numKilled = removedMutants.size();
		Set<Integer> newKilled = new HashSet<Integer>();

		List<TestChromosome> executionOrder = prioritizeTests(suite); // Quicker tests first
		for (TestChromosome test : executionOrder) {
			ExecutionResult result = test.getLastExecutionResult();
			// Using private reflection can lead to false positives
			// that represent unrealistic behaviour. Thus, we only
			// use reflection for basic criteria, not for mutation
			if(result.calledReflection())
				continue;

			ExecutionTrace trace = result.getTrace();
			touchedMutants.addAll(trace.getTouchedMutants());
			logger.debug("Tests touched " + touchedMutants.size() + " mutants");

			for(Integer mutantId : trace.getTouchedMutants()) {
				// Only mutants not in the archive yet
				if(newKilled.contains(mutantId) || !mutants.contains(mutantId) || removedMutants.contains(mutantId))
					continue;
				MutationTestFitness mutantFitness = mutantMap.get(mutantId);

				if (MutationTimeoutStoppingCondition.isDisabled(mutantFitness.getMutation())) {
					logger.debug("Skipping timed out mutation "
					        + mutantFitness.getMutation().getId());
					continue;
				}

				mutantsChecked++;
				double mutantInfectionDistance = trace.getMutationDistance(mutantId);
				if (mutantInfectionDistance == 0.0) {
					logger.debug("Executing test against mutant " + mutantFitness.getMutation());
					double mutantFitnessValue = mutantFitness.getFitness(test, result);
					minMutantFitness.put(mutantFitness.getMutation(),
							Math.min(normalize(mutantFitnessValue), minMutantFitness.get(mutantFitness.getMutation())));
					if (mutantFitnessValue == 0.0) {
						result.test.addCoveredGoal(mutantFitness);
						numKilled++;
						newKilled.add(mutantId);
						if(Properties.TEST_ARCHIVE) {
							toRemoveMutants.add(mutantFitness.getMutation().getId());
							TestsArchive.instance.putTest(this, mutantFitness, result);
							individual.isToBeUpdated(true);
						}
					}
				} else {
					double mutantFitnessValue = 1.0 + normalize(mutantInfectionDistance);
					minMutantFitness.put(mutantFitness.getMutation(),
							Math.min(mutantFitnessValue, minMutantFitness.get(mutantFitness.getMutation())));
				}
			}

		}

		//logger.info("Fitness values for " + minMutantFitness.size() + " mutants");
		for (Double fit : minMutantFitness.values()) {
			fitness += fit;
		}
		
		logger.debug("Mutants killed: {}, Checked: {}, Goals: {})", numKilled, mutantsChecked, mutationGoals.size());
		
		updateIndividual(this, individual, fitness);
		// updateGoals();
		suite.setCoverage(this, 1.0 * numKilled / mutationGoals.size());
		suite.setNumOfCoveredGoals(this, numKilled);
		
		return fitness;
	}

}
