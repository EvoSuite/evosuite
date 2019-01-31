package org.evosuite.performance.strategies;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;

import java.util.List;
import java.util.Set;

/**
 * Giovanni Grano
 * The interface for the strategy that can be used in the dynamic approach to set the distances and sort the population
 * @param <T>
 */
public interface PerformanceStrategy<T extends Chromosome> {

    void setDistances(List<T> front);

    void sort(List<T> front);

    String getName();
}
