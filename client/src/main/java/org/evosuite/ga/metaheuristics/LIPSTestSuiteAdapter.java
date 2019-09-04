package org.evosuite.ga.metaheuristics;

import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.metaheuristics.mosa.MOSA;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testsuite.TestSuiteChromosome;

import java.util.ArrayList;
import java.util.List;

/**
 * An adapter that allows the LIPS algorithm to be used in such contexts where
 * {@code TestSuiteChromosome}s are expected instead of {@code TestChromosome}s.
 */
public class LIPSTestSuiteAdapter extends TestSuiteAdapter<LIPS> {
    public LIPSTestSuiteAdapter(LIPS algorithm) {
        super(algorithm);
    }

    /**
     * This method is used by the Progress Monitor at the and of each generation to show the totol coverage reached by the algorithm.
     * Copied from {@link MOSA#archive}.
     *
     * @return "SuiteChromosome" directly consumable by the Progress Monitor.
     */
    @Override
    public TestSuiteChromosome getBestIndividual() {
        TestSuiteChromosome best = new TestSuiteChromosome();
        for (TestChromosome test : getAlgorithm().getArchive()) {
            best.addTest(test);
        }
        // compute overall fitness and coverage
        double coverage = ((double) getAlgorithm().archive.size()) / ((double) this.fitnessFunctions.size());
        best.setCoverage(getAlgorithm().suiteFitness, coverage);
        best.setFitness(getAlgorithm().suiteFitness,
                this.fitnessFunctions.size() - getAlgorithm().archive.size());
        //suiteFitness.getFitness(best);
        return best;
    }

    @Override
    public List<TestSuiteChromosome> getBestIndividuals() {
        //get final test suite (i.e., non dominated solutions in Archive)
        TestSuiteChromosome bestTestCases = new TestSuiteChromosome();
        for (TestChromosome test : getAlgorithm().getFinalTestSuite()) {
            bestTestCases.addTest(test);
        }
        for (TestFitnessFunction f : getAlgorithm().archive.keySet()) {
            bestTestCases.getCoveredGoals().add(f);
        }
        // compute overall fitness and coverage
        double fitness = this.fitnessFunctions.size() - getAlgorithm().numberOfCoveredTargets();
        double coverage = getAlgorithm().numberOfCoveredTargets() / ((double) this.fitnessFunctions.size());
        bestTestCases.setFitness(getAlgorithm().suiteFitness, fitness);
        bestTestCases.setCoverage(getAlgorithm().suiteFitness, coverage);
        bestTestCases.setNumOfCoveredGoals(getAlgorithm().suiteFitness,
                (int) getAlgorithm().numberOfCoveredTargets());
        bestTestCases.setNumOfNotCoveredGoals(getAlgorithm().suiteFitness,
                (int) (this.fitnessFunctions.size() - getAlgorithm().numberOfCoveredTargets()));

        List<TestSuiteChromosome> bests = new ArrayList<>(1);
        bests.add(bestTestCases);
        return bests;
    }
}
