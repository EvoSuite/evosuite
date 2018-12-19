package org.evosuite.ga.comparators;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;

/**
 * Implements a <code>Comparator</code> based on the performance score for the preference criterion of performance MOSA
 * According to the approach we implement, this comparator check first the fitness (relative to the achieved coverage
 * of the branch); When the coverage its the same, it compares the performance score.
 *
 * @author Annibale Panichella, Giovanni Grano
 */
public class PerformanceIndicatorComparator<T extends Chromosome> implements Comparator<Object> {

    private FitnessFunction<T> objective;
    private Comparator<T> comparator;

    private static final Logger logger = LoggerFactory.getLogger(PreferenceSortingComparator.class);

    public PerformanceIndicatorComparator(FitnessFunction<T> goals) {
        this.objective = goals;
        comparator = new PerformanceScoreComparator<>();
    }

    @Override
    public int compare(Object o1, Object o2) {
        if (o1 == null)
            return 1;
        else if (o2 == null)
            return -1;

        T solution1 = (T) o1;
        T solution2 = (T) o2;

        double val1, val2;
        val1 = solution1.getFitness(objective);
        val2 = solution2.getFitness(objective);
        if (val1 < val2)
            return -1;
        else if (val1 > val2)
            return +1;
        return comparator.compare(solution1, solution2);
    }
}
