package org.evosuite.performance.strategies;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;

import java.util.List;
import java.util.Set;

public interface PerformanceStrategy<T extends Chromosome> {

    void setDistances(List<T> front, Set<FitnessFunction<T>> fitnessFunctions);

    void sort(List<T> front);

    String getName();
}
