package org.evosuite.coverage.mutation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.evosuite.Properties;
import org.evosuite.coverage.archive.TestsArchive;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;

public class ArchiveOnlyMutationSuiteFitness extends MutationSuiteFitness {

	private static final long serialVersionUID = -8194940669364526758L;

	public final Set<Integer> mutants = new HashSet<Integer>();

	public final Set<Integer> removedMutants = new HashSet<Integer>();

	public final Set<Integer> toRemoveMutants = new HashSet<Integer>();

	public final Map<Integer, MutationTestFitness> mutantMap = new HashMap<Integer, MutationTestFitness>();
	
	public ArchiveOnlyMutationSuiteFitness() {
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
		Set<Integer> touchedMutants = new HashSet<Integer>();

		for (ExecutionResult result : results) {
			touchedMutants.addAll(result.getTrace().getTouchedMutants());

			for (Entry<Integer, Double> entry : result.getTrace().getMutationDistances().entrySet()) {
				if(!mutants.contains(entry.getKey()) || removedMutants.contains(entry.getKey()))
					continue;
				if(entry.getValue() == 0.0) {
					result.test.addCoveredGoal(mutantMap.get(entry.getKey()));
					if(Properties.TEST_ARCHIVE) {
						toRemoveMutants.add(entry.getKey());
						TestsArchive.instance.putTest(this, mutantMap.get(entry.getKey()), result.test);
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

    public TestSuiteChromosome getBestStoredIndividual(){
		if(!Properties.TEST_ARCHIVE) {
			return null;
		}
        // TODO: There's a design problem here because
        //       other fitness functions use the same archive
        return TestsArchive.instance.getReducedChromosome();
        //return testArchive.getBestChromosome();
    }
}
