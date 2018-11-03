package org.evosuite.novelty;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.ga.metaheuristics.SearchListener;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class NoveltyFitnessEvaluationListener implements SearchListener {

    private List<TestSuiteFitnessFunction> fitnessFunctions;

    public NoveltyFitnessEvaluationListener(List<TestSuiteFitnessFunction> fitnessFunctions) {
        this.fitnessFunctions = new ArrayList<>(fitnessFunctions);
    }

    public TestSuiteChromosome createMergedSolution(Collection<TestChromosome> population) {
        TestSuiteChromosome suite = new TestSuiteChromosome();
        suite.addTests(population);
        return suite;
    }
    @Override
    public void iteration(GeneticAlgorithm<?> algorithm) {
        List<TestChromosome> population = ((GeneticAlgorithm<TestChromosome>)algorithm).getPopulation();
        TestSuiteChromosome suite = createMergedSolution(population);
        for (FitnessFunction fitnessFunction : fitnessFunctions) {
            fitnessFunction.getFitness(suite);
        }

        // Update fitness functions based on goals just added to archive
        algorithm.updateFitnessFunctionsAndValues();
    }


    @Override
    public void searchStarted(GeneticAlgorithm<?> algorithm) {

    }

    @Override
    public void searchFinished(GeneticAlgorithm<?> algorithm) {

    }

    @Override
    public void fitnessEvaluation(Chromosome individual) {

    }

    @Override
    public void modification(Chromosome individual) {

    }
}
