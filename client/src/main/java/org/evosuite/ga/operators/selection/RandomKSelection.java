package org.evosuite.ga.operators.selection;

import org.evosuite.ga.Chromosome;
import org.evosuite.utils.Randomness;

import java.util.List;

/**
 * Select random individual
 */
public class RandomKSelection<T extends Chromosome<T>> extends SelectionFunction<T> {

    private static final long serialVersionUID = -2459623722712044154L;

    public RandomKSelection() {
    }

    public RandomKSelection(RandomKSelection<?> other) {
        // empty copy constructor
    }

    @Override
    public int getIndex(List<T> population) {
        double r = Randomness.nextDouble();

        return (int) (r * population.size());
    }
}
