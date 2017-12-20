/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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

import java.util.LinkedHashMap;
import java.util.Map;
import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.coverage.branch.BranchCoverageSuiteFitness;
import org.evosuite.ga.archive.Archive;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.TestCase;
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

	protected final Map<Integer, MutationTestFitness> mutantMap = new LinkedHashMap<Integer, MutationTestFitness>();

	protected final int numMutants;

	public MutationSuiteFitness() {
		MutationFactory factory = new MutationFactory(
		        ArrayUtil.contains(Properties.CRITERION, Criterion.STRONGMUTATION));
		branchFitness = new BranchCoverageSuiteFitness();

		for (MutationTestFitness goal : factory.getCoverageGoals()) {
			mutantMap.put(goal.getMutation().getId(), goal);
			if(Properties.TEST_ARCHIVE)
				Archive.getArchiveInstance().addTarget(goal);
		}

		this.numMutants = this.mutantMap.size();
	}

	@Override
	public boolean updateCoveredGoals() {
		if(!Properties.TEST_ARCHIVE)
			return false;

		// TODO as soon the archive refactor is done, we can get rid of this function

		return true;
	}
	
	/** {@inheritDoc} */
	@Override
	public ExecutionResult runTest(TestCase test) {
		return runTest(test, null);
	}

	public int getNumMutants() {
	  return this.numMutants;
	}

	public int howManyMutantsHaveKilled() {
	  return this.numMutants - this.mutantMap.size();
	}

	public boolean allMutantsKilled() {
	  return this.mutantMap.isEmpty();
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
