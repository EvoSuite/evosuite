package org.evosuite.performance.strategies;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;

/**
 * Returns the concrete implementation of <code>IDominanceSorter</code> based on the specified property
 * @author Giovanni Grano
 */
public class DominanceSortingAlgoFactory<T extends Chromosome> {

    public static IDominanceSorter getDominanceSortingAlgorithm() {
        switch (Properties.P_DOMINANCE_SORTING) {
            case DOMINANCE:
                return new PerformanceDominanceSorter();
            case FAST_DOMINANCE:
                return new FastPerformanceDominanceSorter();
            case EPSILON_DOMINANCE:
                return new EpsilonPerformanceDominanceSorter();
            default:
                throw new RuntimeException("This sorting algorithm for the dominance is not implemented");
        }
    }
}
