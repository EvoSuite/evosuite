package org.evosuite.ga.comparators;

import org.evosuite.ga.Chromosome;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Comparator that sorts a collection of chromosomes according to the performance score
 *
 * @author Giovanni Grano
 */
public class PerformanceScoreComparator<T extends Chromosome> implements Comparator<T>, Serializable {
    
    @Override
    public int compare(T o1, T o2) {
        if (o1.getPerformanceScore() > o2.getPerformanceScore())
            return 1;
        else if (o1.getPerformanceScore() < o2.getPerformanceScore())
            return -1;
        else
            return 0;
    }
}
