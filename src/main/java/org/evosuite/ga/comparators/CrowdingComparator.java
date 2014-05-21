package org.evosuite.ga.comparators;

import java.util.Comparator;

import org.evosuite.ga.Chromosome;

public class CrowdingComparator
    implements Comparator<Chromosome>
{
    private boolean isToMaximize;

    public CrowdingComparator(boolean maximize) {
        this.isToMaximize = maximize;
    }

    @Override
    public int compare(Chromosome c1, Chromosome c2)
    {
        if (c1.getRank() == c2.getRank() && c1.getDistance() == c2.getDistance())
            return 0;

        if (this.isToMaximize) {
            if (c1.getRank() < c2.getRank())
                return 1;
            else if (c1.getRank() > c2.getRank())
                return -1;
            else if (c1.getRank() == c2.getRank())
                return (c1.getDistance() > c2.getDistance()) ? -1 : 1;
        }
        else {
            if (c1.getRank() < c2.getRank())
                return -1;
            else if (c1.getRank() > c2.getRank())
                return 1;
            else if (c1.getRank() == c2.getRank())
                return (c1.getDistance() > c2.getDistance()) ? -1 : 1;
        }

        return 0;
    }
}
