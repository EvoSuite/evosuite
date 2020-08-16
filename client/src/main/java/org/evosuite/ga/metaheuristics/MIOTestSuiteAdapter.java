package org.evosuite.ga.metaheuristics;

import org.evosuite.ga.archive.Archive;
import org.evosuite.ga.metaheuristics.mosa.MOSATestSuiteAdapter;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MIOTestSuiteAdapter extends MOSATestSuiteAdapter {
    private static final long serialVersionUID = 3297246957849185819L;

    public MIOTestSuiteAdapter(final MIO algorithm) {
        super(algorithm);
    }

    @Override
    public List<TestSuiteChromosome> getBestIndividuals() {
        // get final test suite (i.e., non dominated solutions in Archive)
        TestSuiteChromosome bestTestCases = new TestSuiteChromosome();
        Set<TestChromosome> solutions = Archive.getArchiveInstance().getSolutions();
        bestTestCases.addTests(solutions);

        // compute overall fitness and coverage
        this.computeCoverageAndFitness(bestTestCases);

        List<TestSuiteChromosome> bests = new ArrayList<>(1);
        bests.add(bestTestCases);

        return bests;
    }

    @Override
    public TestSuiteChromosome getBestIndividual() {
        TestSuiteChromosome best = new TestSuiteChromosome();
        Set<TestChromosome> solutions = Archive.getArchiveInstance().getSolutions();
        best.addTests(solutions);

        if (solutions.isEmpty()) {
            final Set<TestSuiteFitnessFunction> ffs = getAlgorithm().suiteFitnessFunctions.keySet();
            for (TestSuiteFitnessFunction suiteFitness : ffs) {
                best.setCoverage(suiteFitness, 0.0);
                best.setFitness(suiteFitness,  1.0);
            }
            return best;
        }

        // compute overall fitness and coverage
        this.computeCoverageAndFitness(best);

        return best;
    }
}
