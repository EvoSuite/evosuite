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
package org.evosuite.ga.metaheuristics;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;

import java.util.Objects;

/**
 * Search Listener to redirect Notifications from an adaptee algorithm to an adapter algorithm.
 * <p>
 * Evaluation and Mutation notifications are disabled by default.
 */
public class AdapteeListener implements SearchListener<TestChromosome> {

    private final GeneticAlgorithm<TestSuiteChromosome> adapter;
    private final boolean notifyEvaluation;
    private final boolean notifyMutation;

    public AdapteeListener(GeneticAlgorithm<TestSuiteChromosome> adapter, boolean notifyEvaluation,
                           boolean notifyMutation) {
        this.adapter = Objects.requireNonNull(adapter);
        this.notifyEvaluation = notifyEvaluation;
        this.notifyMutation = notifyMutation;
    }

    public AdapteeListener(GeneticAlgorithm<TestSuiteChromosome> adapter) {
        this(adapter, false, false);
    }


    @Override
    public void searchStarted(GeneticAlgorithm<TestChromosome> algorithm) {
        adapter.notifySearchStarted();
    }

    @Override
    public void iteration(GeneticAlgorithm<TestChromosome> algorithm) {
        adapter.notifyIteration();
    }

    @Override
    public void searchFinished(GeneticAlgorithm<TestChromosome> algorithm) {
        adapter.notifySearchFinished();
    }

    @Override
    public void fitnessEvaluation(TestChromosome individual) {
        // The adapter throws currently a exception when notifying a evaluation
        if (notifyEvaluation)
            adapter.notifyEvaluation(individual.toSuite());
    }

    @Override
    public void modification(TestChromosome individual) {
        // The adapter throws currently a exception when notifying a mutation
        if (notifyMutation)
            adapter.notifyMutation(individual.toSuite());
    }
}
