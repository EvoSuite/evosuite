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
package org.evosuite.testsuite;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.ga.operators.mutation.MutationDistribution;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * @param <E> Class for SelfTyped Pattern
 */
public abstract class AbstractTestSuiteChromosome<T extends AbstractTestSuiteChromosome<T, E>,
        E extends ExecutableChromosome<E>> extends Chromosome<T> {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(AbstractTestSuiteChromosome.class);

    //
    protected List<E> tests = new ArrayList<>();
    protected ChromosomeFactory<E> testChromosomeFactory;

    /**
     * only used for testing/debugging
     */
    protected AbstractTestSuiteChromosome() {
        super();
    }


    /**
     * <p>Constructor for AbstractTestSuiteChromosome.</p>
     *
     * @param testChromosomeFactory a {@link org.evosuite.ga.ChromosomeFactory} object.
     */
    protected AbstractTestSuiteChromosome(ChromosomeFactory<E> testChromosomeFactory) {
        this.testChromosomeFactory = testChromosomeFactory;
    }

    /**
     * <p>Getter for the field <code>testChromosomeFactory</code>.</p>
     *
     * @return a {@link org.evosuite.ga.ChromosomeFactory} object.
     */
    public ChromosomeFactory<? extends E> getTestChromosomeFactory() {
        return testChromosomeFactory;
    }

    /**
     * Creates a deep copy of source.
     *
     * @param source a {@link org.evosuite.testsuite.AbstractTestSuiteChromosome} object.
     */
    protected AbstractTestSuiteChromosome(T source) {
        this(source.testChromosomeFactory);

        source.tests.forEach(e -> addTest(e.clone()));

        //this.setFitness(source.getFitness());
        this.setFitnessValues(source.getFitnessValues());
        this.setPreviousFitnessValues(source.getPreviousFitnessValues());
        this.setChanged(source.isChanged());
        this.setCoverageValues(source.getCoverageValues());
        this.setNumsOfCoveredGoals(source.getNumsOfCoveredGoals());
        this.setNumsOfNotCoveredGoals(source.getNumsNotCoveredGoals());
        this.setNumberOfMutations(source.getNumberOfMutations());
        this.setNumberOfEvaluations(source.getNumberOfEvaluations());
        this.setKineticEnergy(source.getKineticEnergy());
        this.setNumCollisions(source.getNumCollisions());
    }

    /**
     * <p>addTest</p>
     *
     * @param test a T object.
     */
    public void addTest(E test) {
        tests.add(test);
        this.setChanged(true);
    }

    public void deleteTest(E test) {
        boolean changed = tests.remove(test);
        if (changed)
            this.setChanged(true);
    }

    public abstract E addTest(TestCase testCase);

    /**
     * <p>addTests</p>
     *
     * @param tests a {@link java.util.Collection} object.
     */
    public void addTests(Collection<E> tests) {
        this.tests.addAll(tests);
        if (!tests.isEmpty())
            this.setChanged(true);
    }

    public abstract void addTestChromosome(TestChromosome testChromosome);

    public void addTestChromosomes(Collection<TestChromosome> testChromosomes) {
        testChromosomes.forEach(this::addTestChromosome);
    }

    /**
     * <p>addUnmodifiableTest</p>
     *
     * @param test a T object.
     */
    public void addUnmodifiableTest(E test) {
        tests.add(test);
        this.setChanged(true);
    }


    /**
     * {@inheritDoc}
     * <p>
     * Replace chromosome at position
     */
    @Override
    public void crossOver(T other, int position) throws ConstructionFailedException {
        E otherTest = other.self().tests.get(position);
        E clonedTest = otherTest.clone().self();
        tests.add(clonedTest);

        this.setChanged(true);
    }


    /**
     * {@inheritDoc}
     * <p>
     * Keep up to position1, append copy of other from position2 on
     */
    @Override
    public void crossOver(T other, int position1, int position2)
            throws ConstructionFailedException {

        while (tests.size() > position1) {
            tests.remove(position1);
        }

        for (int num = position2; num < other.size(); num++) {
            E otherTest = other.self().tests.get(num);
            E clonedTest = otherTest.clone().self();
            tests.add(clonedTest);
        }

        this.setChanged(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (!(obj instanceof AbstractTestSuiteChromosome))
            return false;
        if (!obj.getClass().isInstance(this.getClass()))
            return false;
        TestSuiteChromosome other = (TestSuiteChromosome) obj;
        if (other.size() != size())
            return false;

        return tests.equals(other.tests);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return tests.hashCode();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Apply mutation on test suite level
     */
    @Override
    public void mutate() {
        boolean changed = false;

        MutationDistribution probabilityDistribution = MutationDistribution.getMutationDistribution(tests.size());

        // Mutate existing test cases
        for (int i = 0; i < tests.size(); i++) {
            E test = tests.get(i);
            if (probabilityDistribution.toMutate(i)) {
                test.mutate();
                if (test.isChanged())
                    changed = true;
            }
        }

        // Add new test cases
        final double ALPHA = Properties.P_TEST_INSERTION; //0.1;

        for (int count = 1; Randomness.nextDouble() <= Math.pow(ALPHA, count)
                && size() < Properties.MAX_SIZE; count++) {
            E test = testChromosomeFactory.getChromosome();
            addTest(test);
            logger.debug("Adding new test case");
            changed = true;
        }

        tests.removeIf(test -> test.size() == 0);

        if (changed) {
            this.increaseNumberOfMutations();
            this.setChanged(true);
        }
    }

    /**
     * <p>totalLengthOfTestCases</p>
     *
     * @return Sum of the lengths of the test cases
     */
    public int totalLengthOfTestCases() {
        return tests.stream().mapToInt(E::size).sum();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return tests.size();
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public abstract T clone();

    /**
     * <p>getTestChromosome</p>
     *
     * @param index a int.
     * @return a T object.
     */
    public E getTestChromosome(int index) {
        return tests.get(index);
    }

    /**
     * <p>getTestChromosomes</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<E> getTestChromosomes() {
        return tests;
    }

    public void replaceTests(List<E> newTests) {
        tests.clear();
        tests.addAll(newTests);
    }

    public void replaceWithTestChromosomes(List<TestChromosome> newTests) {
        tests.clear();
        addTestChromosomes(newTests);
    }

    public List<ExecutionResult> getLastExecutionResults() {
        return tests.stream()
                .map(E::getLastExecutionResult)
                .collect(toList());
    }

    /**
     * <p>setTestChromosome</p>
     *
     * @param index a int.
     * @param test  a T object.
     */
    public void setTestChromosome(int index, E test) {
        tests.set(index, test);
        this.setChanged(true);
    }

    /**
     * Remove all tests
     */
    public void clearTests() {
        tests.clear();
    }
}
