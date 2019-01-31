package org.evosuite.performance.strategies;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.comparators.OnlyCrowdingComparator;
import org.evosuite.performance.comparator.IndicatorComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Giovanni Grano, Annibale Panichella
 *
 * Implements a strategy to include the performance indicators in MOSA.
 * In this approach, we replace the crowding distance with the score obtained by
 * the combination of the performance scores. In this implementation of
 * {#link PerformanceStrategy}, the performance indicat core
 */
public class IndicatorComparisonStrategy<T extends Chromosome> implements PerformanceStrategy<T> {

    private static final Logger logger = LoggerFactory.getLogger(IndicatorComparisonStrategy.class);

    private IndicatorComparator comparator = new IndicatorComparator();

    /**
     * Computes the performance scores for the front and set the distance to - the score itself
     * @param front
     *          the front for which we have to set the distances to
     */
    @Override
    public void setDistances(List<T> front) {
        for (T individual : front) {
            individual.setDistance(0.0);
        }

        for(String ind : front.get(0).getIndicatorValues().keySet()){
            logger.debug("Indicator = {}", ind);
            comparator.setIndicator(ind);
            Double min = Collections.min(front, comparator).getIndicatorValue(ind);
            Double max = Collections.max(front, comparator).getIndicatorValue(ind);

            for (T individual : front) {
                if (Double.compare(min, max) != 0) {
                    double currentValue = individual.getIndicatorValue(ind);
                    double normalizedValue = (max - currentValue) / (max - min);
                    logger.debug("{}, {}, {}, {} >> {}", ind, min, max, currentValue, normalizedValue);
                    individual.setDistance(individual.getDistance() + normalizedValue);
                } else {
                    /* min and max are the same! We cannot compute the indicator here!!!
                    We add 0.5 in those cases */
                    logger.debug("Same min and max while computing the value for {}" + ind);
                    individual.setDistance(individual.getDistance() + 0.5);
                }
            }
        }
    }

    @Override
    /**
     * Sorts the front like using the crowding distance
     */
    public void sort(List<T> front) {
        Collections.sort(front, new OnlyCrowdingComparator());
    }

    @Override
    public String getName() {
        return "CROWDING_DISTANCE";
    }

}
