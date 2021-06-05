package org.evosuite.ga.comparators;

import org.evosuite.ga.FitnessFunction;
import org.evosuite.testcase.TestChromosome;

import java.util.Comparator;

/**
 * Implements a <code>Comparator</code> based on the performance score for the preference criterion of performance MOSA
 * According to the approach we implement, this comparator check first the fitness (relative to the achieved coverage
 * of the branch); When the coverage its the same, it compares the performance score.
 *
 * @author Annibale Panichella, Giovanni Grano
 */
public class PerformanceIndicatorComparator implements Comparator<TestChromosome> {

    private final FitnessFunction<TestChromosome> objective;
    private final Comparator<TestChromosome> comparator;

    public PerformanceIndicatorComparator(FitnessFunction<TestChromosome> goals) {
        this.objective = goals;
        comparator = new PerformanceScoreComparator();
    }

    @Override
    public int compare(TestChromosome o1, TestChromosome o2) {
        if (o1 == null)
            return 1;
        else if (o2 == null)
            return -1;

        double val1, val2;
        val1 = o1.getFitness(objective);
        val2 = o2.getFitness(objective);
        if (val1 < val2)
            return -1;
        else if (val1 > val2)
            return +1;
        return comparator.compare(o1, o2);
    }
}
