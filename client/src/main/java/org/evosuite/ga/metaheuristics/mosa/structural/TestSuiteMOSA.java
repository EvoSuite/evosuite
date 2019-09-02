package org.evosuite.ga.metaheuristics.mosa.structural;

import junit.framework.TestSuite;
import org.evosuite.Properties;
import org.evosuite.coverage.FitnessFunctions;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.archive.Archive;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.ga.metaheuristics.mosa.AbstractMOSA;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.evosuite.utils.BudgetConsumptionMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class TestSuiteMOSA extends GeneticAlgorithm<TestSuiteChromosome,
        FitnessFunction<TestSuiteChromosome>> {

    /**
     * Constructor
     *
     * @param factory a {@link ChromosomeFactory} object.
     */
    public TestSuiteMOSA(ChromosomeFactory<TestSuiteChromosome> factory) {
        super(factory);
    }


//    private static final Logger logger = LoggerFactory.getLogger(TestSuiteMOSA.class);
//
//    /** Keep track of overall suite fitness functions and correspondent test fitness functions */
//    protected final Map<FitnessFunction<TestSuiteChromosome>, Class<?>> suiteFitnessFunctions;
//    /** Object used to keep track of the execution time needed to reach the maximum coverage */
//    protected final BudgetConsumptionMonitor budgetMonitor;
//
//    protected TestSuiteMOSA(ChromosomeFactory<TestSuiteChromosome> factory) {
//        super(factory);
//
//        this.suiteFitnessFunctions = new LinkedHashMap<>();
//        for (Properties.Criterion criterion : Properties.CRITERION) {
//            TestSuiteFitnessFunction suiteFit = FitnessFunctions.getFitnessFunction(criterion);
//            Class<?> testFit = FitnessFunctions.getTestFitnessFunctionClass(criterion);
//            this.suiteFitnessFunctions.put(suiteFit, testFit);
//        }
//
//        this.budgetMonitor = new BudgetConsumptionMonitor();
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public void initializePopulation() {
//        logger.info("executing initializePopulation function");
//
//        this.notifySearchStarted();
//        this.currentIteration = 0;
//
//        // Create a random parent population P0
//        this.generateInitialPopulation(Properties.POPULATION);
//
//        // Determine fitness
//        this.calculateFitness();
//        this.notifyIteration();
//    }
//
//    protected void computeCoverageAndFitness(TestSuiteChromosome suite) {
//      for (Map.Entry<TestSuiteFitnessFunction, Class<?>> entry : this.suiteFitnessFunctions
//          .entrySet()) {
//        TestSuiteFitnessFunction suiteFitnessFunction = entry.getKey();
//        Class<?> testFitnessFunction = entry.getValue();
//
//        int numberCoveredTargets =
//            Archive.getArchiveInstance().getNumberOfCoveredTargets(testFitnessFunction);
//        int numberUncoveredTargets =
//            Archive.getArchiveInstance().getNumberOfUncoveredTargets(testFitnessFunction);
//        int totalNumberTargets = numberCoveredTargets + numberUncoveredTargets;
//
//        double coverage = totalNumberTargets == 0 ? 1.0
//            : ((double) numberCoveredTargets) / ((double) totalNumberTargets);
//
//        suite.setFitness(suiteFitnessFunction, ((double) numberUncoveredTargets));
//        suite.setCoverage(suiteFitnessFunction, coverage);
//        suite.setNumOfCoveredGoals(suiteFitnessFunction, numberCoveredTargets);
//        suite.setNumOfNotCoveredGoals(suiteFitnessFunction, numberUncoveredTargets);
//      }
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @SuppressWarnings("unchecked")
//    @Override
//    public List getBestIndividuals() {
//        // get final test suite (i.e., non dominated solutions in Archive)
//        TestSuiteChromosome bestTestCases = Archive.getArchiveInstance().mergeArchiveAndSolution(new TestSuiteChromosome());
//        if (bestTestCases.getTestChromosomes().isEmpty()) {
//          for (TestChromosome test : this.getNonDominatedSolutions(this.population)) {
//            bestTestCases.addTest(test);
//          }
//        }
//
//        // compute overall fitness and coverage
//        this.computeCoverageAndFitness(bestTestCases);
//
//		return Collections.singletonList((ExecutableChromosome) bestTestCases);
//    }
//
//    /**
//     * Generates a {@link TestSuiteChromosome} object with all test cases
//     * in the archive.
//     *
//     * @return
//     */
//    protected abstract TestSuiteChromosome generateSuite();
}
