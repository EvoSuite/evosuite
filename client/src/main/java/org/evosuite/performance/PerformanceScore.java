package org.evosuite.performance;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.metaheuristics.mosa.PerformanceDynaMOSA;
import org.evosuite.performance.comparator.MinMaxCalculator;
import org.evosuite.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * This class sets the value of the performance score according to the combination strategy chosen
 *
 * @author Giovanni Grano
 */
public class PerformanceScore<T extends Chromosome> {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceScore.class);

    public void assignPerformanceScore(List<T> population) {
        switch (Properties.P_COMBINATION_STRATEGY) {
            case SUM:
                LoggingUtils.getEvoLogger().info("DynaMOSA does not currently support SUM as a combination strategy");
                throw new RuntimeException("Not supported in this version of DYNAMOSA");
            case MIN_MAX:
                MinMaxCalculator<T> indicator = new MinMaxCalculator<>();
                // computes the min-max scores stored in an ad-hoc variable in the chromosome
                indicator.computeIndicatorMinMaxSum(population);
                // move the computed min-max scores in the performance score variable
                // it was done like that cause we wanted to use the performance score to keep also information
                // about the dominance ranking computed with the performance score
                for (T individual : population) {
                    individual.setPerformanceScore(individual.getMinMaxSum());
                    logger.debug("Individual score = {}", individual.getPerformanceScore());
                }
                break;
            case DOMINANCE:
                LoggingUtils.getEvoLogger().info("DynaMOSA does not currently support DOMINANCE as a combination " +
                        "strategy");
                throw new RuntimeException("Not supported in this version of DYNAMOSA");
        }
    }
}
