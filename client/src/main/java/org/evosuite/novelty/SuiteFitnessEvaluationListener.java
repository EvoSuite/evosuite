/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * <p>
 * This file is part of EvoSuite.
 * <p>
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 * <p>
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.novelty;

import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.ga.metaheuristics.SearchListener;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SuiteFitnessEvaluationListener implements SearchListener<TestChromosome> {

    private static final long serialVersionUID = 3871230464292232335L;

    private final List<TestSuiteFitnessFunction> fitnessFunctions;

    public SuiteFitnessEvaluationListener(List<TestSuiteFitnessFunction> fitnessFunctions) {
        this.fitnessFunctions = new ArrayList<>(fitnessFunctions);
    }

    public SuiteFitnessEvaluationListener(SuiteFitnessEvaluationListener that) {
        this(that.fitnessFunctions);
    }

    private TestSuiteChromosome createMergedSolution(Collection<TestChromosome> population) {
        TestSuiteChromosome suite = new TestSuiteChromosome();
        suite.addTests(population);
        return suite;
    }

    public TestSuiteChromosome getSuiteWithFitness(GeneticAlgorithm<TestChromosome> algorithm) {
        List<TestChromosome> population = algorithm.getPopulation();
        TestSuiteChromosome suite = createMergedSolution(population);

        for (TestSuiteFitnessFunction fitnessFunction : fitnessFunctions) {
            fitnessFunction.getFitness(suite);
        }

        return suite;
    }

    @Override
    public void iteration(GeneticAlgorithm<TestChromosome> algorithm) {
        getSuiteWithFitness(algorithm);

        // Update fitness functions based on goals just added to archive
        algorithm.updateFitnessFunctionsAndValues();
    }


    @Override
    public void searchStarted(GeneticAlgorithm<TestChromosome> algorithm) {

    }

    @Override
    public void searchFinished(GeneticAlgorithm<TestChromosome> algorithm) {

    }

    @Override
    public void fitnessEvaluation(TestChromosome individual) {

    }

    @Override
    public void modification(TestChromosome individual) {

    }
}
