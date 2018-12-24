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
     * Computes for every chromosome the performance score as the sum of the indicators normalized via min-max
     * normalization. This value is stored in the minMaxSum variable for each chromosome.
     * @param solutions
     *      solutions to calculate
     */
    public void computeIndicatorMinMaxSum(List<T> solutions) {
        for (T individual : solutions) {
            individual.setMinMaxSum(1.0);
        }

        for(String ind : solutions.get(0).getIndicatorValues().keySet()){
            logger.debug("Indicator = {}", ind);
            comparator.setIndicator(ind);
            Double min = Collections.min(solutions, comparator).getIndicatorValue(ind);
            Double max = Collections.max(solutions, comparator).getIndicatorValue(ind);

            for (T individual : solutions) {
                if (Double.compare(min, max) != 0) {
                    double currentValue = individual.getIndicatorValue(ind);
                    double normalizedValue = (max - currentValue) / (max - min);
                    logger.debug("{}, {}, {}, {} >> {}", ind, min, max, currentValue, normalizedValue);
                    individual.setMinMaxSum(individual.getMinMaxSum() + normalizedValue);
                } else {
                    /* min and max are the same! We cannot compute the indicator here!!!
                    We add 0.5 in those cases */
                    logger.error("Same min and max while computing the value for {}" + ind);
                    individual.setMinMaxSum(individual.getMinMaxSum() + 0.5);
                }
            }
        }
    }
}
