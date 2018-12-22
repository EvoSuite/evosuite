package org.evosuite.performance.strategies;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.comparators.OnlyCrowdingComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private OnlyCrowdingComparator comparator = new OnlyCrowdingComparator();
    /**
     * Computes the performance scores for the front and set the distance to - the score itself
     * @param front
     *          the front for which we have to set the distances to
     */
    @Override
    public void setDistances(List<T> front, Set<FitnessFunction<T>> fitnessFunctions) {
        logger.debug("Assigning performance score as distance");
        front.forEach(individual -> individual.setDistance(-individual.getPerformanceScore()));
    }

    @Override
    public void sort(List<T> front) {
        Collections.sort(front, comparator);
    }

    @Override
    public String getName() {
        return "CROWDING_DISTANCE";
    }

}
