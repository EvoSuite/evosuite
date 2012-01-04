/**
 * 
 */
package de.unisb.cs.st.evosuite.coverage.mutation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.unisb.cs.st.evosuite.coverage.branch.BranchCoverageSuiteFitness;
import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.ExecutionTrace;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteChromosome;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteFitnessFunction;

/**
 * @author Gordon Fraser
 * 
 */
public class MutationSuiteFitness extends TestSuiteFitnessFunction {

	private static final long serialVersionUID = -8320078404661057113L;

	private final BranchCoverageSuiteFitness branchFitness;

	private final List<TestFitnessFunction> mutationGoals;

	public static int mostCoveredGoals = 0;

	public MutationSuiteFitness() {
		MutationFactory factory = new MutationFactory();
		mutationGoals = factory.getCoverageGoals();
		logger.info("Mutation goals: " + mutationGoals.size());
		branchFitness = new BranchCoverageSuiteFitness();
	}

	@Override
	public ExecutionResult runTest(TestCase test) {
		return runTest(test, null);
	}

	public ExecutionResult runTest(TestCase test, Mutation mutant) {

		return MutationTestFitness.runTest(test, mutant);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.FitnessFunction#getFitness(de.unisb.cs.st.evosuite.ga.Chromosome)
	 */
	@Override
	public double getFitness(Chromosome individual) {
		runTestSuite((TestSuiteChromosome) individual);

		Set<MutationTestFitness> uncoveredMutants = MutationTestPool.getUncoveredFitnessFunctions();

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
		double fitness = normalize(branchFitness.getFitness(individual));
		//double fitness = branchFitness.getFitness(individual);
		//logger.info("Branch fitness: " + fitness);

		// Additional objective 1: all mutants need to be touched

		// Count number of touched mutations (ask MutationObserver)
		TestSuiteChromosome suite = (TestSuiteChromosome) individual;
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

			if (result.hasTimeout()) {
				logger.debug("Skipping test with timeout");
				continue;
			}

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
		logger.info("Mutants killed: {} (Checked: {})", numKilled, mutantsChecked);

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
