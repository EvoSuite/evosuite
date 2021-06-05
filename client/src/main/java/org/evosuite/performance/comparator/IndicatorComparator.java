package org.evosuite.performance.comparator;

import org.evosuite.ga.Chromosome;
import org.evosuite.testcase.TestChromosome;

import java.io.Serializable;
import java.util.Comparator;

public class IndicatorComparator  implements Comparator<Chromosome>, Serializable {

    private String indicator_id;

    public void setIndicator(String ind){
        this.indicator_id = ind;
    }

    @Override
    public int compare(Chromosome o1, Chromosome o2) {
        TestChromosome tch1 = (TestChromosome) o1;
        TestChromosome tch2 = (TestChromosome) o2;
        double val1 = tch1.getIndicatorValue(indicator_id);
        double val2 = tch2.getIndicatorValue(indicator_id);
        if (val1 < val2)
            return -1;
        else if (val1 > val2)
            return +1;
        else
            return 0;

    }
}
