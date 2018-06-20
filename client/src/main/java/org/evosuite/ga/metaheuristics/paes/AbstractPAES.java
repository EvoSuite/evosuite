package org.evosuite.ga.metaheuristics.paes;

import org.evosuite.Properties;
import org.evosuite.coverage.FitnessFunctions;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Sebastian on 20.06.2018.
 */
public abstract class AbstractPAES<C extends Chromosome> extends GeneticAlgorithm<C>{

    // archive to store non-dominated solutions
    protected Archive<C> archive;
    /** Keep track of overall suite fitness functions and correspondent test fitness functions */
    protected final Map<TestSuiteFitnessFunction, Class<?>> suiteFitnessFunctions;

    /**
     * Constructor
     *
     * @param factory a {@link ChromosomeFactory} object.
     */
    public AbstractPAES(ChromosomeFactory<C> factory) {
        super(factory);
        this.suiteFitnessFunctions = new LinkedHashMap<>();
        for (Properties.Criterion criterion : Properties.CRITERION) {
            TestSuiteFitnessFunction suiteFit = FitnessFunctions.getFitnessFunction(criterion);
            Class<?> testFit = FitnessFunctions.getTestFitnessFunctionClass(criterion);
            this.suiteFitnessFunctions.put(suiteFit, testFit);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initializePopulation() {
        C first = this.chromosomeFactory.getChromosome();
        this.calculateFitness(first);
        this.population = new ArrayList<>();
        this.population.add(first);
        this.archive = new MyArchive<>(first.getCoverageValues().keySet(), 0, 1);
    }

    @Override
    public C getBestIndividual(){
        TestSuiteChromosome best = generateSuite();
        if(best.getTestChromosomes().isEmpty()) {
            for (TestSuiteFitnessFunction suiteFitness : this.suiteFitnessFunctions.keySet()) {
                best.setCoverage(suiteFitness, 0.0);
                best.setFitness(suiteFitness, 1.0);
            }
            return (C) best;
        }
        this.computeCoverageAndFitness(best);
        return (C)best;
    }

    private void computeCoverageAndFitness(TestSuiteChromosome suite){
        for (Map.Entry<TestSuiteFitnessFunction, Class<?>> entry : this.suiteFitnessFunctions
                .entrySet()) {
            TestSuiteFitnessFunction suiteFitnessFunction = entry.getKey();
            Class<?> testFitnessFunction = entry.getValue();

            int numberCoveredTargets = 0;//TODO Wert berechnen
            int numberUncoveredTargets = 0;//TODO Wert berechnen

            suite.setFitness(suiteFitnessFunction, ((double) numberUncoveredTargets));
            suite.setCoverage(suiteFitnessFunction, ((double) numberCoveredTargets)
                    / ((double) (numberCoveredTargets + numberUncoveredTargets)));
            suite.setNumOfCoveredGoals(suiteFitnessFunction, numberCoveredTargets);
            suite.setNumOfNotCoveredGoals(suiteFitnessFunction, numberUncoveredTargets);
        }
    }

    protected TestSuiteChromosome generateSuite(){
        if(this.population.isEmpty())
            this.initializePopulation();
        TestSuiteChromosome testSuiteChromosome = new TestSuiteChromosome();
        testSuiteChromosome.addTest((TestChromosome)this.population.get(0));
        for(C test : this.archive.getChromosomes())
            testSuiteChromosome.addTest((TestChromosome) test);
        return testSuiteChromosome;
    }
}
