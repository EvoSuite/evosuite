package org.evosuite.performance.strategies;

import org.evosuite.ga.Chromosome;

import java.util.List;

/**
 * Interface that handles the sorting mechanisms with the dominance combination
 * @author Sebastiano Panichella
 * @param <T>
 *     a <code>Chromosome</code>
 */
public interface IDominanceSorter<T extends Chromosome> {

    List<T> getSortedWithIndicatorsDominance(List<T> population);

    String nameAlgo();
}
