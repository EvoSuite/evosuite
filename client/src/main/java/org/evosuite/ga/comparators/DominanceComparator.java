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
import org.evosuite.ga.FitnessFunction;

/**
 * Sort a Collection of Chromosomes by their Dominance
 * 
 * @author José Campos
 */
public class DominanceComparator
    implements Comparator<Chromosome>, Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = -3962098107892633870L;

    /**
     * Is c2 dominated by c1?
     * 
     * http://en.wikipedia.org/wiki/Multi-objective_optimization#Introduction
     * 
     * @param c1
     * @param c2
     * @return -1, or 0, or 1 if solution1 dominates solution2, both are non-dominated, or solution1 is dominated by
     *         solution2, respectively.
     */
    @Override
    public int compare(Chromosome c1, Chromosome c2)
    {
        int dominate1 = 0;
        int dominate2 = 0;

        int flag; // stores the result of the comparison

        for (FitnessFunction<?> ff : c1.getFitnessValues().keySet()) {
            double value1 = c1.getFitness(ff);
            double value2 = c2.getFitness(ff);

            if (value1 < value2)
                flag = -1;
            else if (value1 > value2)
                flag = 1;
            else
                flag = 0;

            if (flag == -1)
                dominate1 = 1;
            if (flag == 1)
                dominate2 = 1;
        }

        if (dominate1 == dominate2)
            return 0; // no one dominate the other
        if (dominate1 == 1)
            return -1; // chromosome1 dominate

        return 1; // chromosome2 dominate
    }
}
