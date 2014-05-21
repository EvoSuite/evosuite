package org.evosuite.ga.comparators;

import java.util.Comparator;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;

public class SortByFitness
    implements Comparator<Chromosome>
{
    /**
     * 
     */
    private boolean ascendingOrder;

    /**
     * 
     */
    private FitnessFunction<?> ff;

    /**
     * 
     * @param ascendingOrder
     * @param ff
     */
    public SortByFitness(boolean ascendingOrder, FitnessFunction<?> ff) {
        this.ascendingOrder = ascendingOrder;
        this.ff = ff;
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

        if (this.ascendingOrder) {
            if (objetive1 < objetive2)
                return -1;
            else if (objetive1 > objetive2)
                return 1;
            else
                return 0;
        }
        else {
            if (objetive1 < objetive2)
                return 1;
            else if (objetive1 > objetive2)
                return -1;
            else
                return 0;
        }
    }
}
