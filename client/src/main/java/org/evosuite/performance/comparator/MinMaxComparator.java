package org.evosuite.performance.comparator;

import org.evosuite.ga.Chromosome;

import java.io.Serializable;
import java.util.Comparator;

public class MinMaxComparator implements Comparator<Chromosome>, Serializable {

    @Override
    public int compare(Chromosome o1, Chromosome o2) {
        double normalizedSum1 = o1.getMinMaxSum();
        double normalizedSum2 = o2.getMinMaxSum();

        if (normalizedSum1 == normalizedSum2)
            return 0;

        return (normalizedSum1 < normalizedSum2 ? -1 : 1);
    }
}
