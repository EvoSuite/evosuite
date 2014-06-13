package org.evosuite.ga.comparators;

import java.util.Comparator;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;

/**
 * Sort a Collection of Chromosomes by their fitness value
 * 
 * @author José Campos
 */
public class SortByFitness
    implements Comparator<Chromosome>
{
    private FitnessFunction<?> ff;

    private boolean order;

    /**
     * 
     * @param ff
     * @param des descending order
     */
    public SortByFitness(FitnessFunction<?> ff, boolean desc) {
        this.ff = ff;
        this.order = desc;
    }

    @Override
    public int compare(Chromosome c1, Chromosome c2)
    {
        if (c1 == null)
            return 1;
        else if (c2 == null)
            return -1;

        double objetive1 = c1.getFitness(this.ff);
        double objetive2 = c2.getFitness(this.ff);

        if (this.order) {
            if (objetive1 < objetive2)
                return 1;
            else if (objetive1 > objetive2)
                return -1;
            else
                return 0;
        }
        else {
            if (objetive1 < objetive2)
                return -1;
            else if (objetive1 > objetive2)
                return 1;
            else
                return 0;
        }
    }
}
