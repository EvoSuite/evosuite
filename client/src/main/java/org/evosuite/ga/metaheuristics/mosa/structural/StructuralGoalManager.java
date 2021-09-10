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
package org.evosuite.ga.metaheuristics.mosa.structural;

import org.evosuite.ga.archive.Archive;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A class for managing coverage targets based on structural dependencies. More specifically,
 * control dependence information of the UIT is used to derive the set of targets currently aimed
 * at. Also maintains an archive of the best chromosomes satisfying a given coverage goal.
 *
 * @author Annibale Panichella
 */
public abstract class StructuralGoalManager implements Serializable {

    private static final long serialVersionUID = -2577487057354286024L;

    /**
     * Set of goals currently used as objectives.
     * <p>
     * The idea is to consider only those gaols that are independent from any other targets. That
     * is, the gaols that
     * <ol>
     *     <li>are free of control dependencies, or</li>
     *     <li>only have direct control dependencies to already covered gaols.</li>
     * </ol>
     * <p>
     * Each goal is encoded by a corresponding fitness function, which returns an optimal fitness value if the goal has been reached by a given
     * chromosome. All functions are required to be either minimization or maximization functions,
     * not a mix of both.
     */
    protected Set<TestFitnessFunction> currentGoals;

    /**
     * Archive of tests and corresponding covered targets
     */
    protected Archive archive;

    /**
     * Creates a new {@code StructuralGoalManager} with the given list of targets.
     *
     * @param fitnessFunctions The targets to cover, with each individual target encoded as its own
     *                         fitness function.
     */
    protected StructuralGoalManager(List<TestFitnessFunction> fitnessFunctions) {
        currentGoals = new HashSet<>(fitnessFunctions.size());
        archive = Archive.getArchiveInstance();

        // initialize uncovered goals
        this.archive.addTargets(fitnessFunctions);
    }

    /**
     * Update the set of covered goals and the set of current goals (actual objectives)
     *
     * @param c a TestChromosome
     * @return covered goals along with the corresponding test case
     */
    public abstract void calculateFitness(TestChromosome c,
                                          GeneticAlgorithm<TestChromosome> ga);

    /**
     * Returns the set of yet uncovered goals.
     *
     * @return uncovered goals
     */
    public Set<TestFitnessFunction> getUncoveredGoals() {
        return this.archive.getUncoveredTargets();
    }

    /**
     * Returns the subset of uncovered goals that are currently targeted. Each such goal has a
     * direct control dependency to one of the already covered goals.
     *
     * @return all currently targeted goals
     */
    public Set<TestFitnessFunction> getCurrentGoals() {
        return currentGoals;
    }

    /**
     * Returns the set of already covered goals.
     *
     * @return the covered goals
     */
    public Set<TestFitnessFunction> getCoveredGoals() {
        return this.archive.getCoveredTargets();
    }

    /**
     * Tells whether an individual covering the given target is already present in the archive.
     *
     * @param target the goal to be covered
     * @return {@code true} if the archive contains a chromosome that covers the target
     */
    protected boolean isAlreadyCovered(TestFitnessFunction target) {
        return this.archive.getCoveredTargets().contains(target);
    }

    /**
     * Records that the given coverage goal is satisfied by the given chromosome.
     *
     * @param f  the coverage goal to be satisfied
     * @param tc the chromosome satisfying the goal
     */
    protected void updateCoveredGoals(TestFitnessFunction f, TestChromosome tc) {
        // the next two lines are needed since that coverage information are used
        // during EvoSuite post-processing
        tc.getTestCase().getCoveredGoals().add(f);

        // update covered targets
        this.archive.updateArchive(f, tc, tc.getFitness(f));
    }
}
