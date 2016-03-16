/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.ga.comparators;

import java.io.Serializable;
import java.util.Comparator;

import org.evosuite.ga.Chromosome;

/**
 * Sort a Collection of Chromosomes by Crowd
 * 
 * @author José Campos
 */
public class CrowdingComparator
    implements Comparator<Chromosome>, Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = -6576898111709166470L;

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
