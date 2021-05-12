package org.evosuite.performance.strategies;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.utils.LoggingUtils;

/**
 * @author Giovanni Grano
 *
 * Returns a <code>PerformanceStrategyFactor</code> concrete class
 * The returned concrete object does dipend on the strategy used to integrate the performance indicators in MOSA
 * A <code>IndicatorComparisonStrategy</code> is returned if we use the indicators instead of the crowding distance
 *
 */
public class PerformanceStrategyFactory<T extends Chromosome> {

    public static PerformanceStrategy getPerformanceStrategy() {
        if (Properties.P_STRATEGY == Properties.PerformanceMOSAStrategy.CROWDING_DISTANCE) {
            LoggingUtils.getEvoLogger().info("* Running Adaptive DYNAMOSA with CROWDING DISTANCE strategy");
            return new IndicatorComparisonStrategy();
        }
        throw new RuntimeException("Such a strategy for the PerformanceMOSA algorithm is not supported");
    }
}
