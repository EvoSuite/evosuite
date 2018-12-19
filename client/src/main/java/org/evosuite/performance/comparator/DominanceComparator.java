package org.evosuite.performance.comparator;

import org.evosuite.ga.Chromosome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Comparator;
import java.util.LinkedHashMap;

/**
 * @author Giovanni Grano, Sebastiano Panichella
 *
 * Compares two chromosome based on the dominance of their performance indicators.
 * A chromosome dominates the other if it has at least one smaller value of indicators and none of the others greater
 */
public class DominanceComparator implements Comparator<Chromosome>, Serializable {

    private static final Logger logger = LoggerFactory.getLogger(DominanceComparator.class);

    /**
     * Returns -1 if chromosome 1 dominates chromosome 2, i.e., pushed for the next generation
     *
     * @param o1 the new chromosome
     * @param o2 the old chromosome
     *
     */
    @Override
    public int compare(Chromosome o1, Chromosome o2) {

        LinkedHashMap<String, Double> valuesOne = o1.getIndicatorValues();
        LinkedHashMap<String, Double> valuesTwo = o2.getIndicatorValues();

        int dominates1 = 0, dominates2=0;
        for (String key : valuesOne.keySet()) {
            if (Double.compare(valuesOne.get(key), valuesTwo.get(key))==-1)
                dominates1 = 1;
            else if (Double.compare(valuesOne.get(key), valuesTwo.get(key))==+1)
                dominates2 = 1;
        }

        if (dominates1 > dominates2)
            return -1;
        else if(dominates1 < dominates2)
            return +1;
        return 0;

    }
}
