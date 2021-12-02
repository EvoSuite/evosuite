package org.evosuite.performance.comparator;

import org.evosuite.testcase.TestChromosome;

import java.io.Serializable;
import java.util.Comparator;

/**
 * @author Giovanni Grano
 *
 * Compares two chromosomes taking into account the sum of their performance indicators
 */
public class SumComparator implements Comparator<TestChromosome>, Serializable {

    @Override
    public int compare(TestChromosome c1, TestChromosome c2) {
        double c1Sum = c1.getIndicatorValues().values().stream().mapToDouble(Number::doubleValue).sum();
        double c2Sum = c2.getIndicatorValues().values().stream().mapToDouble(Number::doubleValue).sum();

        if (c1Sum == c2Sum)
            return 0;

        return (c1Sum < c2Sum ? -1 : 1);
    }
}
