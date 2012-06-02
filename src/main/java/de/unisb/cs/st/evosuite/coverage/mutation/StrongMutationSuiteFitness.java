/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package de.unisb.cs.st.evosuite.coverage.mutation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.ExecutionTrace;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteChromosome;

/**
 * @author fraser
 * 
 */
public class StrongMutationSuiteFitness extends MutationSuiteFitness {

	private static final long serialVersionUID = -9124328839917834720L;

	@Override
	public ExecutionResult runTest(TestCase test) {
		return runTest(test, null);
	}

	@Override
	public ExecutionResult runTest(TestCase test, Mutation mutant) {

		return StrongMutationTestFitness.runTest(test, mutant);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.FitnessFunction#getFitness(de.unisb.cs.st.evosuite.ga.Chromosome)
	 */
	@Override
	public double getFitness(Chromosome individual) {
		runTestSuite((TestSuiteChromosome) individual);

		Set<MutationTestFitness> uncoveredMutants = MutationTestPool.getUncoveredFitnessFunctions();
		TestSuiteChromosome suite = (TestSuiteChromosome) individual;

		for (TestChromosome test : suite.getTestChromosomes()) {
			ExecutionResult result = test.getLastExecutionResult();

			if (result.hasTimeout()) {
				logger.debug("Skipping test with timeout");
				double fitness = branchFitness.totalBranches * 2
				        + branchFitness.totalMethods + 3 * mutationGoals.size();
				updateIndividual(individual, fitness);
				suite.setCoverage(0.0);
				logger.info("Test case has timed out, setting fitness to max value "
				        + fitness);
				return fitness;
			}
		}

		/*
		Set<TestFitnessFunction> coveredMutants = ((TestSuiteChromosome) individual).getCoveredGoals();
		logger.info("Test suite covers mutants: {}", coveredMutants.size());
		Set<TestFitnessFunction> uncoveredMutants = new HashSet<TestFitnessFunction>();
		for (TestFitnessFunction mutation : mutationGoals) {
			if (!coveredMutants.contains(mutation))
				uncoveredMutants.add(mutation);
		}
		*/

		// First objective: achieve branch coverage
		logger.debug("Calculating branch fitness: ");
		//double fitness = normalize(branchFitness.getFitness(individual));
		double fitness = branchFitness.getFitness(individual);
		//logger.info("Branch fitness: " + fitness);

		// Additional objective 1: all mutants need to be touched

		// Count number of touched mutations (ask MutationObserver)

		Set<Integer> touchedMutants = new HashSet<Integer>();
		//Map<Integer, Double> infectionDistance = new HashMap<Integer, Double>();
		Map<Mutation, Double> minMutantFitness = new HashMap<Mutation, Double>();
		for (TestFitnessFunction mutant : uncoveredMutants) {
			MutationTestFitness mutantFitness = (MutationTestFitness) mutant;
			minMutantFitness.put(mutantFitness.getMutation(), 3.0);
		}
		//Set<TestChromosome> safeCopies = new HashSet<TestChromosome>();
		int mutantsChecked = 0;
		for (TestChromosome test : suite.getTestChromosomes()) {
			ExecutionResult result = test.getLastExecutionResult();
			ExecutionTrace trace = result.getTrace();
			touchedMutants.addAll(trace.touchedMutants);

			boolean coversNewMutants = false;
			for (TestFitnessFunction mutant : uncoveredMutants) {

				MutationTestFitness mutantFitness = (MutationTestFitness) mutant;
				if (MutationTimeoutStoppingCondition.isDisabled(mutantFitness.getMutation())) {
					logger.debug("Skipping timed out mutation "
					        + mutantFitness.getMutation().getId());
					continue;
				}
				if (MutationTestPool.isCovered(mutantFitness.getMutation()))
					continue;

				if (trace.touchedMutants.contains(mutantFitness.getMutation().getId())) {
					mutantsChecked++;
					logger.debug("Executing test against mutant "
					        + mutantFitness.getMutation());
					double mutantFitnessValue = mutant.getFitness(test, result);
					minMutantFitness.put(mutantFitness.getMutation(),
					                     Math.min(normalize(mutantFitnessValue),
					                              minMutantFitness.get(mutantFitness.getMutation())));
					if (mutantFitnessValue == 0.0) {
						MutationTestPool.addTest(mutantFitness.getMutation(), test);
						coversNewMutants = true;
						break;
					}
					//fitness += FitnessFunction.normalize(mutantFitnessValue);
				}// else
				 //fitness += 1.0;

			}
			//if (coversNewMutants) {
			//	safeCopies.add((TestChromosome) test.clone());
			//}

			/*
						for (Entry<Integer, Double> mutation : trace.mutant_distances.entrySet()) {
							if (!infectionDistance.containsKey(mutation.getKey()))
								infectionDistance.put(mutation.getKey(), mutation.getValue());
							else
								infectionDistance.put(mutation.getKey(),
								                      Math.min(mutation.getValue(),
								                               infectionDistance.get(mutation.getKey())));
						}
						*/
		}

		//for (TestChromosome copy : safeCopies) {
		//	suite.addUnmodifiableTest(copy);
		//}
		int coverage = ((TestSuiteChromosome) individual).getCoveredGoals().size();

		if (mostCoveredGoals < coverage)
			mostCoveredGoals = coverage;

		//logger.info("Fitness values for " + minMutantFitness.size() + " mutants");
		int numKilled = MutationTestPool.getCoveredMutants();
		for (Double fit : minMutantFitness.values()) {
			//if (fit == 0.0)
			//	numKilled++;
			fitness += fit;
		}
		fitness += mutationGoals.size() - MutationTestPool.getCoveredMutants();
		logger.debug("Mutants killed: {} (Checked: {})", numKilled, mutantsChecked);

		/*
		logger.info("Touched " + touchedMutants.size() + " / " + numMutations
		        + " mutants");
		fitness += numMutations - touchedMutants.size();

		// TODO: Always execute on all mutants, or first optimize for branch coverage?
		// Idea: Always calculate infection distance, and only execute again if touched && infection distance = 0.0

		// Additional objective 2: minimize infection distance for all mutants
		double infection = 0.0;
		for (Integer mutationId : infectionDistance.keySet())
			infection += infectionDistance.get(mutationId);

		logger.info("Infection distance: " + infection);
		fitness += infection;

		// Additional objective 3: each mutant needs to propagate
		// fitness += total mutants - killed mutants

		// TODO: Each statement needs to be covered at least as often as it is mutated? No.
		*/
		updateIndividual(individual, fitness);
		suite.setCoverage(1.0 * numKilled / mutationGoals.size());

		return fitness;
	}
}
