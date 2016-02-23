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
/**
 * 
 */
package org.evosuite.coverage.mutation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.coverage.archive.TestsArchive;
import org.evosuite.coverage.branch.BranchCoverageSuiteFitness;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.evosuite.utils.ArrayUtil;

/**
 * <p>
 * Abstract MutationSuiteFitness class.
 * </p>
 * 
 * @author Gordon Fraser
 */
public abstract class MutationSuiteFitness extends TestSuiteFitnessFunction {

	private static final long serialVersionUID = -8320078404661057113L;

	protected final BranchCoverageSuiteFitness branchFitness;

	protected final List<MutationTestFitness> mutationGoals;

	public final Set<Integer> mutants = new HashSet<Integer>();

	public final Set<Integer> removedMutants = new HashSet<Integer>();

	public final Set<Integer> toRemoveMutants = new HashSet<Integer>();

	public final Map<Integer, MutationTestFitness> mutantMap = new HashMap<Integer, MutationTestFitness>();

	public MutationSuiteFitness() {
		MutationFactory factory = new MutationFactory(
		        ArrayUtil.contains(Properties.CRITERION, Criterion.STRONGMUTATION));
		mutationGoals = factory.getCoverageGoals();
		logger.info("Mutation goals: " + mutationGoals.size());
		branchFitness = new BranchCoverageSuiteFitness();
		
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
	
	/** {@inheritDoc} */
	@Override
	public ExecutionResult runTest(TestCase test) {
		return runTest(test, null);
	}

	public int getNumMutants() {
		return mutationGoals.size();
	}

	/**
	 * <p>
	 * runTest
	 * </p>
	 * 
	 * @param test
	 *            a {@link org.evosuite.testcase.TestCase} object.
	 * @param mutant
	 *            a {@link org.evosuite.coverage.mutation.Mutation} object.
	 * @return a {@link org.evosuite.testcase.execution.ExecutionResult} object.
	 */
	public ExecutionResult runTest(TestCase test, Mutation mutant) {

		return MutationTestFitness.runTest(test, mutant);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.FitnessFunction#getFitness(org.evosuite.ga.Chromosome)
	 */
	/** {@inheritDoc} */
	@Override
	public abstract double getFitness(
	        AbstractTestSuiteChromosome<? extends ExecutableChromosome> individual);
}
