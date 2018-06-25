package org.evosuite.ga.metaheuristics;

import org.evosuite.Properties;
import org.evosuite.coverage.FitnessFunctions;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.archive.Archive;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Sebastian on 22.06.2018.
 */
public abstract class AbstractMOSuiteGA<T extends Chromosome> extends GeneticAlgorithm<T>{
    /** Keep track of overall suite fitness functions and correspondent test fitness functions */
    protected final Map<TestSuiteFitnessFunction, Class<?>> suiteFitnessFunctions;

    public AbstractMOSuiteGA(ChromosomeFactory<T> factory){
        super(factory);
        this.suiteFitnessFunctions = new LinkedHashMap<>();
        for(Properties.Criterion criterion : Properties.CRITERION){
            TestSuiteFitnessFunction suiteFit = FitnessFunctions.getFitnessFunction(criterion);
            Class<?> testFit = FitnessFunctions.getTestFitnessFunctionClass(criterion);
            this.suiteFitnessFunctions.put(suiteFit, testFit);
        }
    }

    /**
     * This method extracts non-dominated solutions (tests) according to all covered goal
     * (e.g., branches).
     *
     * @param solutions a list of test cases to analyze with the "dominance" relationship
     * @return the non-dominated set of test cases
     */
    public abstract List<T> getNonDominatedSolutions(List<T> solutions);

    /**
     * Generates a {@link org.evosuite.testsuite.TestSuiteChromosome} object with all test cases
     * in the archive.
     *
     * @return
     */
    protected TestSuiteChromosome generateSuite() {
        TestSuiteChromosome suite = new TestSuiteChromosome();
        Archive.getArchiveInstance().getSolutions().forEach(test -> suite.addTest(test));
        return suite;
    }

    /**
     * {@inheritDoc}
     *
     * <p>This method is used by the Progress Monitor at the and of each generation to show the total coverage reached by the algorithm.
     * Since the Progress Monitor requires a {@link org.evosuite.testsuite.TestSuiteChromosome} object, this method artificially creates
     * a {@link org.evosuite.testsuite.TestSuiteChromosome} object as the union of all solutions stored in the {@link
     * org.evosuite.ga.archive.Archive}.</p>
     *
     * <p>The coverage score of the {@link org.evosuite.testsuite.TestSuiteChromosome} object is given by the percentage of targets marked
     * as covered in the archive.</p>
     *
     * @return a {@link org.evosuite.testsuite.TestSuiteChromosome} object to be consumable by the Progress Monitor.
     */
    @SuppressWarnings("unchecked")
    @Override
    public T getBestIndividual(){
        TestSuiteChromosome best = this.generateSuite();
        if (best.getTestChromosomes().isEmpty()) {
            for (T test : this.getNonDominatedSolutions(this.population)) {
                best.addTest((TestChromosome) test);
            }
            for (TestSuiteFitnessFunction suiteFitness : this.suiteFitnessFunctions.keySet()) {
                best.setCoverage(suiteFitness, 0.0);
                best.setFitness(suiteFitness,  1.0);
            }
            return (T) best;
        }

        // compute overall fitness and coverage
        this.computeCoverageAndFitness(best);

        return (T) best;
    }

    private void computeCoverageAndFitness(TestSuiteChromosome suite){
        for (Map.Entry<TestSuiteFitnessFunction, Class<?>> entry : this.suiteFitnessFunctions
                .entrySet()) {
            TestSuiteFitnessFunction suiteFitnessFunction = entry.getKey();
            Class<?> testFitnessFunction = entry.getValue();

            int numberCoveredTargets =
                    Archive.getArchiveInstance().getNumberOfCoveredTargets(testFitnessFunction);
            int numberUncoveredTargets =
                    Archive.getArchiveInstance().getNumberOfUncoveredTargets(testFitnessFunction);

            suite.setFitness(suiteFitnessFunction, ((double) numberUncoveredTargets));
            suite.setCoverage(suiteFitnessFunction, ((double) numberCoveredTargets)
                    / ((double) (numberCoveredTargets + numberUncoveredTargets)));
            suite.setNumOfCoveredGoals(suiteFitnessFunction, numberCoveredTargets);
            suite.setNumOfNotCoveredGoals(suiteFitnessFunction, numberUncoveredTargets);
        }
    }


}
