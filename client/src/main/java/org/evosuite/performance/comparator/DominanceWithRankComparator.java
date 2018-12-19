package org.evosuite.performance.comparator;

import org.evosuite.ga.Chromosome;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Compares two solutions according to their performance rank which is computed by the
 * <code>PerformanceDominanceSorter</code> class;
 * Therefore, this comparator needs to be with the following definition of dominance:
 * "a test case A dominates a test case B iff every indicators for A is lg every indicator for B"
 */
public class DominanceWithRankComparator implements Comparator<Chromosome>, Serializable {

    @Override
    public int compare(Chromosome o1, Chromosome o2) {
        int rank1 = o1.getPerformance_rank();
        int rank2 = o2.getPerformance_rank();

        if (rank1 == rank2)
            return 0;

        return (o1.getPerformance_rank() < o2.getPerformance_rank() ? -1 : 1);
    }
}
