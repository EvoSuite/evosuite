package org.evosuite.ga.operators.selection;

import org.evosuite.ga.Chromosome;
import org.evosuite.utils.Randomness;

import java.util.List;

/**
 * Select random individual
 */
public class RandomKSelection<T extends Chromosome> extends SelectionFunction<T> {

    @Override
    public int getIndex(List<T> population) {
        double r = Randomness.nextDouble();

        return (int) (r * population.size());
    }
}
