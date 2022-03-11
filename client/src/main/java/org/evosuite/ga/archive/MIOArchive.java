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
package org.evosuite.ga.archive;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.evosuite.Properties;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;

import static java.util.stream.Collectors.*;

/**
 * Implementation of the archive described in the 'Many Independent Objective (MIO) Algorithm for
 * Test Suite Generation' paper.
 *
 * @author José Campos
 */
public class MIOArchive extends Archive {

    private static final long serialVersionUID = -6100903230303784634L;

    private static final Logger logger = LoggerFactory.getLogger(MIOArchive.class);

    /**
     * Map used to store all targets (keys of the map) and the corresponding covering solutions
     * (values of the map)
     **/
    protected final Map<TestFitnessFunction, Population> archive = new LinkedHashMap<>();

    public static final MIOArchive instance = new MIOArchive();

    /**
     * {@inheritDoc}
     */
    @Override
    public void addTarget(TestFitnessFunction target) {
        super.addTarget(target);

        if (!this.archive.containsKey(target)) {
            logger.debug("Registering new target '" + target + "'");
            this.archive.put(target, new Population(Properties.NUMBER_OF_TESTS_PER_TARGET));
        }

        this.registerNonCoveredTargetOfAMethod(target);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateArchive(TestFitnessFunction target, TestChromosome solution, double fitnessValue) {
        super.updateArchive(target, solution, fitnessValue);
        assert this.archive.containsKey(target);

        ExecutionResult executionResult = solution.getLastExecutionResult();
        // remove all statements after an exception
        if (!executionResult.noThrownExceptions()) {
            solution.getTestCase().chop(executionResult.getFirstPositionOfThrownException() + 1);
        }

        boolean isNewCoveredTarget = this.archive.get(target)
                .addSolution(1.0 - FitnessFunction.normalize(fitnessValue), solution);
        if (isNewCoveredTarget) {
            this.removeNonCoveredTargetOfAMethod(target);
            this.hasBeenUpdated = true;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isArchiveEmpty() {
        return this.getNumberOfSolutions() == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfTargets() {
        return this.archive.keySet().size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfCoveredTargets() {
        return this.getCoveredTargets().size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfCoveredTargets(Class<?> targetClass) {
        return (int) this.getCoveredTargets().stream()
                .filter(target -> target.getClass() == targetClass).count();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<TestFitnessFunction> getCoveredTargets() {
        return this.archive.keySet().stream()
                .filter(target -> this.archive.get(target).isCovered())
                .collect(toSet());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfUncoveredTargets() {
        return this.getUncoveredTargets().size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfUncoveredTargets(Class<?> targetClass) {
        return (int) this.getUncoveredTargets().stream()
                .filter(target -> target.getClass() == targetClass)
                .count();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<TestFitnessFunction> getUncoveredTargets() {
        return this.archive.keySet().stream()
                .filter(target -> !this.archive.get(target).isCovered())
                .collect(toSet());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasTarget(TestFitnessFunction target) {
        assert target != null;
        return this.archive.containsKey(target);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfSolutions() {
        return this.getSolutions().size();
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public Set<TestChromosome> getSolutions() {
        return this.archive.values().stream()
                .map(Population::getBestSolutionIfAny)
                .filter(Objects::nonNull)
                .collect(toCollection(LinkedHashSet::new));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestChromosome getSolution() {

        // Choose one target at random that has not been covered but contains some solutions. In case
        // there is not any non-covered target with at least one solution, either because all targets
        // have been covered or for the non-covered targets there is not any solution yet, then choose
        // one of the covered targets at random. Thereafter, choose one solution randomly from the list
        // of solutions of the chosen target.

        List<TestFitnessFunction> targetsWithSolutions = this.archive.keySet().stream()
                .filter(target -> this.archive.get(target).numSolutions() > 0).collect(toList());

        if (targetsWithSolutions.isEmpty()) {
            // there is not at least one target with at least one solution
            return null;
        }

        List<TestFitnessFunction> potentialTargets = targetsWithSolutions.stream()
                .filter(target -> !this.archive.get(target).isCovered()).collect(toList());

        if (potentialTargets.isEmpty()) {
            potentialTargets =
                    targetsWithSolutions.stream().filter(target -> this.archive.get(target).isCovered())
                            .collect(toList());
        }
        assert !potentialTargets.isEmpty();

        // Instead of choosing a target at random, we choose the one with the lowest counter value.
        // (See Section 3.3 of the paper that describes this archive for more details)

        // F target = Randomness.choice(potentialTargets);
        // T randomSolution = (T) this.archive.get(target).sampleSolution();

        // ASC sort, i.e., from the population with the lowest counter to the population with the
        // highest counter
        potentialTargets.sort(Comparator.comparingInt(f -> archive.get(f).counter()));

        TestChromosome randomSolution = this.archive.get(potentialTargets.get(0)).sampleSolution();
        return randomSolution == null ? null : randomSolution.clone();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestChromosome getSolution(TestFitnessFunction target) {
        assert target != null;
        assert this.archive.containsKey(target);
        return this.archive.get(target).getBestSolutionIfAny();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasSolution(TestFitnessFunction target) {
        assert target != null;
        assert this.archive.containsKey(target);
        return this.archive.get(target).isCovered();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestChromosome getRandomSolution() {
        return Randomness.choice(this.getSolutions());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected TestSuiteChromosome createMergedSolution(TestSuiteChromosome solution) {
        // Deactivate in case a test is executed and would access the archive as this might cause a
        // concurrent access
        Properties.TEST_ARCHIVE = false;

        TestSuiteChromosome mergedSolution = solution.clone();

        // to avoid adding the same solution to 'mergedSolution' suite
        Set<TestChromosome> solutionsSampledFromArchive = new LinkedHashSet<>();

        for (TestFitnessFunction target : this.archive.keySet()) {
            // does solution cover target?
            if (!target.isCoveredBy(mergedSolution)) {
                Population population = this.archive.get(target);

                // is there any solution in the archive that covers it?
                TestChromosome t = population.getBestSolutionIfAny();
                if (t != null) {
                    // has t been considered?
                    if (!solutionsSampledFromArchive.contains(t)) {
                        solutionsSampledFromArchive.add(t);

                        TestChromosome tClone = t.clone();
                        mergedSolution.addTest(tClone);
                    }
                }
            }
        }

        // re-evaluate merged solution
        for (FitnessFunction<TestSuiteChromosome> ff : solution.getFitnessValues().keySet()) {
            ff.getFitness(mergedSolution);
        }

        // re-active it
        Properties.TEST_ARCHIVE = true;

        logger.info("Final test suite size from archive: " + mergedSolution);
        return mergedSolution;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shrinkSolutions(int newPopulationSize) {
        assert newPopulationSize > 0;
        for (TestFitnessFunction target : this.archive.keySet()) {
            this.archive.get(target).shrinkPopulation(newPopulationSize);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "NumTargets: " + this.getNumberOfTargets() + ", NumCoveredTargets: "
                + this.getNumberOfCoveredTargets() + ", NumSolutions: " + this.getNumberOfSolutions();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        super.reset();
        this.archive.clear();
    }

    private class Population implements Serializable {

        private static final long serialVersionUID = 1671692598239736237L;

        private int counter = 0;

        private int capacity;

        private List<Pair<Double, TestChromosome>> solutions = null;

        /**
         * @param populationSize
         */
        private Population(int populationSize) {
            this.capacity = populationSize;
            this.solutions = new ArrayList<>(populationSize);
        }

        /**
         * @return
         */
        private int counter() {
            return this.counter;
        }

        /**
         * @return
         */
        private boolean isCovered() {
            return this.solutions.size() == 1 && this.capacity == 1
                    && this.solutions.get(0).getLeft() == 1.0;
        }

        /**
         * @param h [0,1] value, where 1 means that the target is covered, and whereas 0 is the worst
         *          possible heuristics value
         * @param t
         */
        private boolean addSolution(Double h, TestChromosome t) {
            assert h >= 0.0 && h <= 1.0;

            if (h == 0.0) {
                // from the paper that describes this type of archive: "if h=0, the test is not added
                // regardless of the following conditions"
                return false;
            }

            if (h < 1.0 && this.isCovered()) {
                // candidate solution T does not cover the already fully covered target, therefore there is
                // no way it could be any better
                return false;
            }

            Pair<Double, TestChromosome> candidateSolution = new ImmutablePair<>(h, t);

            boolean added = false;

            // does the candidate solution fully cover the target?
            if (h == 1.0) {
                // yes. has the target been fully covered by a previous solution?
                if (this.isCovered()) {
                    Pair<Double, TestChromosome> currentSolution = this.solutions.get(0);

                    if (isPairBetterThanCurrent(currentSolution, candidateSolution)) {
                        added = true;
                        this.solutions.set(0, candidateSolution);
                    }
                } else {
                    // as the target is now fully covered by the candidate solution T, from now on there is no
                    // need to keep more than one solution, only the single best one. therefore, we can get
                    // rid of all solutions (if any) and shrink the number of solutions to only one.
                    added = true;
                    this.capacity = 1;
                    this.solutions.clear();
                    this.solutions.add(candidateSolution);
                }
            } else {
                // no, candidate solution T does not fully cover the target.

                // is there enough room for yet another solution?
                if (this.solutions.size() < this.capacity) {
                    // yes, there is.

                    // as an optimisation, in here we could check whether candidateSolution is an existing
                    // solution, however it could be quite expensive to do it and most likely not worth it
                    this.solutions.add(candidateSolution);
                    this.sortPairSolutions(); // keep solutions sorted from the best to the worse
                } else {
                    // no, there is not. so, replace the worst one, if candidate is better.
                    this.sortPairSolutions();
                    Pair<Double, TestChromosome> worstSolution = this.solutions.get(this.capacity - 1);

                    if (isPairBetterThanCurrent(worstSolution, candidateSolution)) {
                        this.solutions.set(this.capacity - 1, candidateSolution);
                    }
                }
            }

            // a set of solutions larger that a maximum capacity would be considered illegal
            assert this.solutions.size() <= this.capacity;

            if (added) {
                // reset counter if and only if a new/better solution has been found
                this.counter = 0;
            }

            return added;
        }

        /**
         * @param currentSolution
         * @param candidateSolution
         * @return
         */
        private boolean isPairBetterThanCurrent(Pair<Double, TestChromosome> currentSolution,
                                                Pair<Double, TestChromosome> candidateSolution) {
            int cmp = Double.compare(currentSolution.getLeft(), candidateSolution.getLeft());
            if (cmp < 0) {
                return true;
            } else if (cmp > 0) {
                return false;
            }
            assert cmp == 0;

            return isBetterThanCurrent(currentSolution.getRight(), candidateSolution.getRight());
        }

        /**
         * @return
         */
        private TestChromosome sampleSolution() {
            if (this.numSolutions() == 0) {
                return null;
            }
            this.counter++;
            return Randomness.choice(this.solutions).getRight();
        }

        /**
         * DESC sort, i.e., from the pair with the highest h to the pair with the lowest h
         */
        private void sortPairSolutions() {
            this.solutions.sort((solution0, solution1) -> {
                if (solution0.getLeft() < solution1.getLeft()) {
                    return 1;
                } else if (solution0.getLeft() > solution1.getLeft()) {
                    return -1;
                }
                return 0;
            });
        }

        /**
         * @return
         */
        private int numSolutions() {
            return this.solutions.size();
        }

        /**
         * @return
         */
        private TestChromosome getBestSolutionIfAny() {
            if (this.numSolutions() == 0 || !this.isCovered()) {
                return null;
            }
            return this.solutions.get(0).getRight();
        }

        /**
         * @param newPopulationSize
         */
        private void shrinkPopulation(int newPopulationSize) {
            assert newPopulationSize > 0;

            if (this.isCovered()) {
                return;
            }

            this.capacity = newPopulationSize;

            if (this.numSolutions() < newPopulationSize) {
                // no need to shrink it
                return;
            }

            List<Pair<Double, TestChromosome>> shrinkSolutions = new ArrayList<>(newPopulationSize);
            for (int i = 0; i < newPopulationSize; i++) {
                shrinkSolutions.add(this.solutions.get(i));
            }
            this.solutions.clear();
            this.solutions.addAll(shrinkSolutions);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return 31 * counter + capacity + this.solutions.hashCode();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (this.getClass() != obj.getClass()) {
                return false;
            }

            Population p = (Population) obj;
            if (this.counter != p.counter) {
                return false;
            }
            if (this.capacity != p.capacity) {
                return false;
            }
            if (this.solutions.size() != p.solutions.size()) {
                return false;
            }

            return this.solutions.equals(p.solutions);
        }
    }
}
