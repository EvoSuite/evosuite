package org.evosuite.ga.metaheuristics;

import org.evosuite.ga.Chromosome;
import org.evosuite.testcase.TestChromosome;

import java.util.List;

/**
 * Interface which declares required functionality by different
 * novelty-metric implementations
 *
 * @author Prathmesh Halgekar
 */
public interface NoveltyMetric {

    /**
     * Calculate distance between two individuals
     * and return a double valued result.
     *
     * @param a individual 1
     * @param b individual 2
     * @return distance between two individuals.
     */
    double calculateDistance(TestChromosome a, TestChromosome b);

    /**
     * Sort the population
     *
     * @param population
     * @return
     */
    void sortPopulation(List<TestChromosome> population);
}
