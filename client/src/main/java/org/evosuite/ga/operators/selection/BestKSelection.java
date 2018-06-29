package org.evosuite.ga.operators.selection;

import org.evosuite.ga.Chromosome;

import java.util.ArrayList;
import java.util.List;

/**
 * {@inheritDoc}
 * 
 * Select individual by highest fitness
 */
public class BestKSelection<T extends Chromosome> extends SelectionFunction<T> {

    /**
     * {@inheritDoc}
     * 
     * Population has to be sorted!
     */
    @Override
    public List<T> select(List<T> population, int number) {
        List<T> offspring = new ArrayList<T>();
        
        int bound = Math.min(number, population.size());
        
        for (int i = 0; i < bound; i++) {
            offspring.add(population.get(i));
        }
        
        return offspring;
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
