package org.evosuite.ga.metaheuristics.mosa;

import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.archive.Archive;
import org.evosuite.ga.metaheuristics.TestSuiteAdapter;
import org.evosuite.ga.metaheuristics.mosa.AbstractMOSA;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * An adapter that allows all variants of the MOSA algorithm to be used in such contexts where
 * {@code TestSuiteChromosome}s are expected instead of {@code TestChromosome}s.
 */
public class MOSATestSuiteAdapter extends TestSuiteAdapter<AbstractMOSA> {

    public MOSATestSuiteAdapter(final AbstractMOSA algorithm) {
        super(algorithm);
        algorithm.setAdapter(this);
    }

    /*
     * Some methods of the super class (i.e., {@link org.evosuite.ga.metaheuristics.GeneticAlgorithm}
	 * class) require a {@link org.evosuite.testsuite.TestSuiteChromosome} object. However, MOSA
	 * evolves {@link org.evosuite.testsuite.TestChromosome} objects. Therefore, we must override
	 * those methods and create a {@link org.evosuite.testsuite.TestSuiteChromosome} object with all
	 * the evolved {@link org.evosuite.testsuite.TestChromosome} objects (either in the population or
     * in the {@link org.evosuite.ga.archive.Archive}).
     *
     * The following code has been copied from org.evosuite.ga.metaheuristics.mosa.AbstractMOSA
     * and retrofitted in a more type-safe fashion.
     */

    @Override
    public List<TestSuiteChromosome> getBestIndividuals() {
        // get final test suite (i.e., non dominated solutions in Archive)
        TestSuiteChromosome bestTestCases = Archive.getArchiveInstance().mergeArchiveAndSolution(new TestSuiteChromosome());
        if (bestTestCases.getTestChromosomes().isEmpty()) {
//            for (TestChromosome test : getAlgorithm().getNonDominatedSolutions(getAlgorithm().population)) {
            for (TestChromosome test : getAlgorithm().getBestIndividuals()) {
                bestTestCases.addTest(test);
            }
        }

        // compute overall fitness and coverage
        this.computeCoverageAndFitness(bestTestCases);

        return Collections.singletonList(bestTestCases);
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
    @Override
    public TestSuiteChromosome getBestIndividual() {
        TestSuiteChromosome best = getAlgorithm().generateSuite();
        if (best.getTestChromosomes().isEmpty()) {
//            for (TestChromosome test : getAlgorithm().getNonDominatedSolutions(getAlgorithm().population)) {
            for (TestChromosome test : getAlgorithm().getBestIndividuals()) {
                best.addTest(test);
            }
            for (TestSuiteFitnessFunction suiteFitness : getAlgorithm().suiteFitnessFunctions.keySet()) {
                best.setCoverage(suiteFitness, 0.0);
                best.setFitness(suiteFitness,  1.0);
            }
            return best;
        }

        // compute overall fitness and coverage
        this.computeCoverageAndFitness(best);

        return best;
    }

    private void computeCoverageAndFitness(TestSuiteChromosome suite) {
        for (Map.Entry<TestSuiteFitnessFunction, Class<?>> entry : getAlgorithm().suiteFitnessFunctions
                .entrySet()) {
            TestSuiteFitnessFunction suiteFitnessFunction = entry.getKey();
            Class<?> testFitnessFunction = entry.getValue();

            int numberCoveredTargets =
                    Archive.getArchiveInstance().getNumberOfCoveredTargets(testFitnessFunction);
            int numberUncoveredTargets =
                    Archive.getArchiveInstance().getNumberOfUncoveredTargets(testFitnessFunction);
            int totalNumberTargets = numberCoveredTargets + numberUncoveredTargets;

            double coverage = totalNumberTargets == 0 ? 1.0
                    : ((double) numberCoveredTargets) / ((double) totalNumberTargets);

            suite.setFitness(suiteFitnessFunction, numberUncoveredTargets);
            suite.setCoverage(suiteFitnessFunction, coverage);
            suite.setNumOfCoveredGoals(suiteFitnessFunction, numberCoveredTargets);
            suite.setNumOfNotCoveredGoals(suiteFitnessFunction, numberUncoveredTargets);
        }
    }
}
