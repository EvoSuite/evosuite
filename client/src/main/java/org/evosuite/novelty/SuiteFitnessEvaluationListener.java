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
