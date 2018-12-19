package org.evosuite.performance.comparator;

import org.evosuite.ga.Chromosome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * @author Giovanni Grano
 *
 * Contains utility methods to compute the min max normalization
 * @param <T>
 */
public class MinMaxCalculator<T extends Chromosome> {

    private static final Logger logger = LoggerFactory.getLogger(MinMaxCalculator.class);

    private IndicatorComparator comparator = new IndicatorComparator();
    /**
     * Computes for every chromosome the min max normalized summed
     * @param solutions
     *      solutions to calculate
     */
    public void computeIndicatorMinMaxSum(List<T> solutions) {
        for (T individual : solutions) {
            individual.setMinMaxSum(1.0);
        }

        for(String ind : solutions.get(0).getIndicatorValues().keySet()){
            comparator.setIndicator(ind);
            Double min = Collections.min(solutions, comparator).getIndicatorValue(ind);
            Double max = Collections.max(solutions, comparator).getIndicatorValue(ind);
            if (Double.compare(min, max) != 0) {
                for (T individual : solutions) {
                    double oldValue = individual.getIndicatorValue(ind);
                    double value = (oldValue - min) / (max - min);
                    //logger.error("{}, {}, {}, {} >> {}", ind, min, max, oldValue, value);
                    individual.setMinMaxSum(individual.getMinMaxSum() + value);
                }
            }
        }

        //for (T individual : solutions) {
        //    logger.error("{}, {}", individual.getIndicatorValues(), individual.getPerformanceScore());
        //}
    }
}
