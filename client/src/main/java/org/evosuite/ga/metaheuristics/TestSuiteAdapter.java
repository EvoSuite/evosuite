package org.evosuite.ga.metaheuristics;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testsuite.TestSuiteChromosome;

import java.util.List;
import java.util.Objects;

/**
 * A wrapper class that facilitates the use of genetic algorithms operating on {@code
 * TestChromosome}s in such contexts where {@code TestSuiteChromosome}s are expected.
 *
 * @param <T> the type of adaptee genetic algorithm
 */
public abstract class TestSuiteAdapter<T extends GeneticAlgorithm<TestChromosome, TestFitnessFunction>> extends GeneticAlgorithm<TestSuiteChromosome,
        FitnessFunction<TestSuiteChromosome>> {

    private final T algorithm;

    /**
     * Constructs a new adapter with the given {@code algorithm} (evolving test chromosomes) as
     * adaptee and the specified factory for test suite chromosomes.
     *
     * @param algorithm the algorithm (operating on {@code TestChromosome}s) to adapt (must not be
     *                  {@code null})
     * @param factory factory for {@code TestSuiteChromosome}s
     */
    TestSuiteAdapter(final T algorithm, final ChromosomeFactory<TestSuiteChromosome> factory) {
        super(factory);
        this.algorithm = Objects.requireNonNull(algorithm);
    }

    /**
     * Returns the wrapped genetic algorithm.
     *
     * @return the wrapped genetic algorithm
     */
    T getAlgorithm() {
        return algorithm;
    }

    @Override
    public abstract TestSuiteChromosome getBestIndividual();

    @Override
    public abstract List<TestSuiteChromosome> getBestIndividuals();

    @Override
    protected void evolve() {
        algorithm.evolve();
    }

    @Override
    public void initializePopulation() {
        algorithm.initializePopulation();
    }

    @Override
    public void generateSolution() {
        algorithm.generateSolution();
    }

    @Override
    public int getAge() {
        return algorithm.getAge();
    }

    @Override
    protected void notifyMutation(Chromosome chromosome) {
        algorithm.notifyMutation(chromosome);
    }

    @Override
    protected void notifyEvaluation(Chromosome chromosome) {
        algorithm.notifyEvaluation(chromosome);
    }

    @Override
    public List getFitnessFunctions() { // FIXME avoid horrible raw return type!!!
        // This method returns a raw List of fitness functions. This is ugly but (at the time of
        // this writing) nothing bad actually happens because MOSuiteStrategy only invokes size()
        // on the returned list.
        return algorithm.getFitnessFunctions();
    }

    @Override
    public void addFitnessFunctions(List functions) { // FIXME avoid horrible raw type!!!
        // The following code still circumvents the type system by using unsafe raw types and
        // unchecked casts. The code only works if a certain assumption holds:
        // MOSuiteStrategy will only ever pass a list of TestFitnessFunctions to this method.
        // In all other cases, this code will blow up. This issue should be fixed as soon as
        // possible, e.g., by creating an adapter class for TestFitnessFunctions to dress up as
        // TestSuiteFitnessFunctions.
        List<TestFitnessFunction> fs = (List<TestFitnessFunction>) functions;

        algorithm.addFitnessFunctions(fs);
    }
}
