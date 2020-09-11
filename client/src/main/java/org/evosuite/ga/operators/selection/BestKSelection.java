package org.evosuite.ga.operators.selection;

import org.evosuite.ga.Chromosome;

import java.util.List;

import static java.util.stream.Collectors.*;

/**
 * {@inheritDoc}
 * 
 * Select individual by highest fitness
 */
public class BestKSelection<T extends Chromosome<T>> extends SelectionFunction<T> {

    private static final long serialVersionUID = -7106376944811871449L;

    public BestKSelection() {
    }

    public BestKSelection(BestKSelection<?> other) {
        // empty copy constructor
    }

    /**
     * {@inheritDoc}
     * 
     * Population has to be sorted!
     */
    @Override
    public List<T> select(List<T> population, int number) {
        return population.stream()
                .limit(number)
                .collect(toList());
    }
    
    /**
     * Selects index of best offspring.
     *
     * Population has to be sorted!
     */
    @Override
    public int getIndex(List<T> population) {
        return 0;
    }
}
