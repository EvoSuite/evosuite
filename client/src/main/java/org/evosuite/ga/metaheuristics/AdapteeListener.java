package org.evosuite.ga.metaheuristics;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;

import java.util.Objects;

/**
 * Search Listener to redirect Notifications from an adaptee algorithm to an adapter algorithm.
 *
 * Evaluation and Mutation notifications are disabled by default.
 */
public class AdapteeListener implements SearchListener<TestChromosome> {

    private final GeneticAlgorithm<TestSuiteChromosome> adapter;
    private final boolean notifyEvaluation;
    private final boolean notifyMutation;

    public AdapteeListener(GeneticAlgorithm<TestSuiteChromosome> adapter, boolean notifyEvaluation,
                           boolean notifyMutation){
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
        if(notifyEvaluation)
            adapter.notifyEvaluation(individual.toSuite());
    }

    @Override
    public void modification(TestChromosome individual) {
        // The adapter throws currently a exception when notifying a mutation
        if(notifyMutation)
            adapter.notifyMutation(individual.toSuite());
    }
}
