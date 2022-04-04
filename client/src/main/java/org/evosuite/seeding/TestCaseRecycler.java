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
package org.evosuite.seeding;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.ga.metaheuristics.SearchListener;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This singleton class serves as a pool for TestChromosomes that are worth
 * recycling
 * <p>
 * Whenever a TestFitnessFunction detects, that a TestChromosome covers it, the
 * TestFitnessFunction will notify this Class by calling
 * testIsInterestingForGoal()
 * <p>
 * Then whenever a genetic algorithm fills it's initial population it will ask
 * this class for interesting TestChromosomes concerning it's current
 * fitness_function getRecycableChromosomes() then returns to the GA a set of
 * all TestChromosomes that were interesting for TestFitnessFunctions that were
 * similar to the given fitness_function - for more information look at
 * TestFitnessFunction.isSimilarTo(), .isCovered() and
 * GeneticAlgorithm.recycleChromosomes()
 *
 * @author Andre Mis
 */
public final class TestCaseRecycler<T extends Chromosome<T>> implements SearchListener<T> {

    private static final long serialVersionUID = -2372656982678139994L;

    private static TestCaseRecycler<?> instance;

    private final Set<TestCase> testPool;

    /**
     * <p>
     * Getter for the field <code>instance</code>.
     * </p>
     *
     * @return a {@link org.evosuite.seeding.TestCaseRecycler} object.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Chromosome<T>> TestCaseRecycler<T> getInstance() {
        if (instance == null)
            instance = new TestCaseRecycler<>();
        return (TestCaseRecycler<T>) instance;
    }

    private TestCaseRecycler() {
        testPool = new LinkedHashSet<>();
    }


    @Override
    public void searchStarted(GeneticAlgorithm<T> algorithm) {
        // TODO Auto-generated method stub

    }

    @Override
    public void iteration(GeneticAlgorithm<T> algorithm) {
        // TODO Auto-generated method stub

    }

    @Override
    public void searchFinished(GeneticAlgorithm<T> algorithm) {
        T individual = algorithm.getBestIndividual();
        if (individual instanceof TestChromosome) {
            TestChromosome testChromosome = (TestChromosome) individual;
            testPool.add(testChromosome.getTestCase());
        } else if (individual instanceof TestSuiteChromosome) {
            TestSuiteChromosome testSuiteChromosome = (TestSuiteChromosome) individual;
            testPool.addAll(testSuiteChromosome.getTests());
        }
    }

    @Override
    public void fitnessEvaluation(T individual) {
        // TODO Auto-generated method stub

    }

    @Override
    public void modification(T individual) {
        // TODO Auto-generated method stub

    }
}
