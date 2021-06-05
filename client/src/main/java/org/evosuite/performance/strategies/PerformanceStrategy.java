package org.evosuite.performance.strategies;

import org.evosuite.testcase.TestChromosome;

import java.util.List;

/**
 * @author Giovanni Grano
 * The interface for the strategy that can be used in the dynamic approach to set the distances and sort the population
 */
public interface PerformanceStrategy {

    void setDistances(List<TestChromosome> front);

    void sort(List<TestChromosome> front);

    String getName();
}
