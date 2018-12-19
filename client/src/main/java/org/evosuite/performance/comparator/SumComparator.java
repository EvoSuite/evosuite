package org.evosuite.performance.comparator;

import org.evosuite.ga.Chromosome;

import java.io.Serializable;
import java.util.Comparator;

/**
 * @author giograno
 *
 * Compares two chromosomes taking into account the sum of their performance indicators
 */
public class SumComparator implements Comparator<Chromosome>, Serializable {

    @Override
    public int compare(Chromosome c1, Chromosome c2) {
        double c1Sum = c1.getIndicatorValues().values().stream().mapToDouble(Number::doubleValue).sum();
        double c2Sum = c2.getIndicatorValues().values().stream().mapToDouble(Number::doubleValue).sum();

        if (c1Sum == c2Sum)
            return 0;

        return (c1Sum < c2Sum ? -1 : 1);
    }
}
