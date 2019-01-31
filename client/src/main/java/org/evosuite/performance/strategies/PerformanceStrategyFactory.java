package org.evosuite.performance.strategies;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.utils.LoggingUtils;

/**
 * @author giograno
 *
 * Returns a <code>PerformanceStrategyFactor</code> concrete class
 * The returned concrete object does dipend on the strategy used to integrate the performance indicators in MOSA
 * A <code>IndicatorComparisonStrategy</code> is returned if we use the indicators instead of the crowding distance;
 * a <code>PreferenceCriterionStrategy</code> is returned if we use the indicators instead of the test case
 * length for the archive
 *
 */
public class PerformanceStrategyFactory<T extends Chromosome> {

    public static PerformanceStrategy getPerformanceStrategy() {
        switch (Properties.P_STRATEGY) {
            case CROWDING_DISTANCE:
                LoggingUtils.getEvoLogger().info("* Running Adaptive DYNAMOSA with CROWDING DISTANCE strategy");
                return new IndicatorComparisonStrategy();
        }
        throw new RuntimeException("Such a strategy for the PerformanceMOSA algorithm is not supported");
    }
}
