package org.evosuite.performance.comparator;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;

import java.util.Comparator;

public class ComparatorFactory {

    public static Comparator<Chromosome> getComparator() {
        if (!Properties.CLIENT_ON_THREAD) {
            // executing the client in the same JVM
            String performance_strategy = System.getProperty("performance_combination_strategy");
            Properties.P_COMBINATION_STRATEGY.valueOf(performance_strategy);
        }

        switch (Properties.P_COMBINATION_STRATEGY) {
            case SUM:
                return new SumComparator();
            case MIN_MAX:
                return new MinMaxComparator();
            case DOMINANCE:
                return new DominanceWithRankComparator();
        }
        throw new RuntimeException("Invalid Performance Indicator Combination Strategy");
    }

    public static boolean isDominance() {
        return (ComparatorFactory.getComparator() instanceof DominanceWithRankComparator ? true : false);
    }
}
