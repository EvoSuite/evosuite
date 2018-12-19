package org.evosuite.performance.strategies;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.comparators.OnlyCrowdingComparator;
import org.evosuite.ga.operators.ranking.CrowdingDistance;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author giograno
 *
 * Implements a strategy to include the performance indicators in MOSA.
 * In this approach, we change the preference criterion. Instead of using the length of a TC during the
 * archive phase, we use the indicators (according to different stategies)
 * The crowding distance approach used in MOSA remains then the same
 */
public class PreferenceCriterionStrategy<T extends Chromosome> implements PerformanceStrategy<T> {

    @Override
    /**
     * Sets the distances in the front exactly the normal implementation in MOSA
     */
    public void setDistances(List<T> front, Set<FitnessFunction<T>> fitnessFunctions) {
        CrowdingDistance<T> distance = new CrowdingDistance<>();
        distance.fastEpsilonDominanceAssignment(front, fitnessFunctions);
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
        return "ARCHIVE_METHOD";
    }

}
