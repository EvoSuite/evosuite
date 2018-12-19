package org.evosuite.performance.comparator;

import org.evosuite.ga.Chromosome;

import java.io.Serializable;
import java.util.Comparator;
import java.util.LinkedHashMap;

/**
 * @author Sebastiano Panichella
 *
 * Compares two chromosome based on the epsilon-dominance of their performance indicators.
 */
public class EpsilonDominanceComparator implements Comparator<Chromosome>, Serializable {

    /**
     * Returns -1 if chromosome 1 epsilon-dominates chromosome 2;
     * 0 if none of the two epsilon-dominates the other;
     * +1 otherwise (i.e., if chromosome 2 epsilon-dominates chromosome 1)
     *
     * @param o1 the new chromosome
     * @param o2 the old chromosome
     *
     */
    @Override
    public int compare(Chromosome o1, Chromosome o2) {
        LinkedHashMap<String, Double> valuesOne = o1.getIndicatorValues();
        LinkedHashMap<String, Double> valuesTwo = o2.getIndicatorValues();

        double diff = 0;
        for (String key : valuesOne.keySet()) {
            diff += valuesOne.get(key) - valuesTwo.get(key);
        }

        if (diff < 0)
            return -1;
        else if (diff>0)
            return +1;
        else
            return 0;
    }
}
