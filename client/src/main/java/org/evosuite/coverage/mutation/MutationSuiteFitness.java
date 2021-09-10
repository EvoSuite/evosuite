/*
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
package org.evosuite.coverage.mutation;

import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.coverage.FitnessFunctions;
import org.evosuite.coverage.branch.BranchCoverageSuiteFitness;
import org.evosuite.ga.archive.Archive;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

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

    // target goals
    protected final Map<Integer, MutationTestFitness> mutantMap = new LinkedHashMap<>();
    protected final int numMutants;

    protected final Set<Integer> removedMutants = new LinkedHashSet<>();
    protected final Set<Integer> toRemoveMutants = new LinkedHashSet<>();

    public MutationSuiteFitness(Criterion criterion) {
        if (criterion != Criterion.STRONGMUTATION && criterion != Criterion.WEAKMUTATION
                && criterion != Criterion.ONLYMUTATION) {
            throw new RuntimeException("Invalid initialisation of MutationSuiteFitness with criterion '"
                    + criterion.name()
                    + "'. MutationSuiteFitness can only be invoked with STRONGMUTATION, WEAKMUTATION, and ONLYMUTATION");
        }
        MutationFactory factory = (MutationFactory) FitnessFunctions.getFitnessFactory(criterion);

        boolean archive = Properties.TEST_ARCHIVE;
        Properties.TEST_ARCHIVE = false;
        branchFitness = new BranchCoverageSuiteFitness();
        Properties.TEST_ARCHIVE = archive;

        for (MutationTestFitness goal : factory.getCoverageGoals()) {
            mutantMap.put(goal.getMutation().getId(), goal);
            if (Properties.TEST_ARCHIVE)
                Archive.getArchiveInstance().addTarget(goal);
        }

        this.numMutants = this.mutantMap.size();
    }

    @Override
    public boolean updateCoveredGoals() {
        if (!Properties.TEST_ARCHIVE) {
            return false;
        }

        for (Integer mutant : this.toRemoveMutants) {
            TestFitnessFunction ff = this.mutantMap.remove(mutant);
            if (ff != null) {
                this.removedMutants.add(mutant);
            } else {
                throw new IllegalStateException("goal to remove not found");
            }
        }

        this.toRemoveMutants.clear();
        logger.info("Current state of archive: " + Archive.getArchiveInstance().toString());

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExecutionResult runTest(TestCase test) {
        return runTest(test, null);
    }

    /**
     * <p>
     * runTest
     * </p>
     *
     * @param test   a {@link org.evosuite.testcase.TestCase} object.
     * @param mutant a {@link org.evosuite.coverage.mutation.Mutation} object.
     * @return a {@link org.evosuite.testcase.execution.ExecutionResult} object.
     */
    public ExecutionResult runTest(TestCase test, Mutation mutant) {

        return MutationTestFitness.runTest(test, mutant);
    }

    /* (non-Javadoc)
     * @see org.evosuite.ga.FitnessFunction#getFitness(org.evosuite.ga.Chromosome)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract double getFitness(TestSuiteChromosome individual);

}
