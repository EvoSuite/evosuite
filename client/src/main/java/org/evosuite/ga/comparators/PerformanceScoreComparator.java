package org.evosuite.ga.comparators;

import org.evosuite.testcase.TestChromosome;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Comparator that sorts a collection of chromosomes according to the performance score
 *
 * @author Giovanni Grano
 */
public class PerformanceScoreComparator implements Comparator<TestChromosome>, Serializable {

    @Override
    public int compare(TestChromosome o1, TestChromosome o2) {
        return Double.compare(o1.getPerformanceScore(), o2.getPerformanceScore());
    }
}
