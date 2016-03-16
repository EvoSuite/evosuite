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
package org.evosuite.ga.operators.selection;

import java.util.List;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.comparators.CrowdingComparator;
import org.evosuite.utils.Randomness;

/**
 * Select an individual from a population using a Crowd Comparison Operator
 * 
 * @author José Campos
 */
public class BinaryTournamentSelectionCrowdedComparison<T extends Chromosome> extends SelectionFunction<T>
{
	private static final long serialVersionUID = -6887165634607218631L;

    /**
     * index stores the actual index for selection
     */
    private int index = 0;

    /**
     * indexes stores a permutation of ints
     */
    private int indexes[];

    private CrowdingComparator comparator;

    public BinaryTournamentSelectionCrowdedComparison() {
        this.comparator = new CrowdingComparator(maximize);
    }

    public BinaryTournamentSelectionCrowdedComparison(boolean isToMaximize) {
        this.comparator = new CrowdingComparator(isToMaximize);
    }

	@Override
	public int getIndex(List<T> population)
	{
	    if (this.index == 0) // Create the permutation
	        this.indexes = intPermutation(population.size());

	    int index1 = this.index;
		T p1 = population.get(this.indexes[index1]);

        int index2 = this.index + 1;
        T p2 = population.get(this.indexes[index2]);

        this.index = (this.index + 2) % (population.size());

        int flag = this.comparator.compare(p1, p2);
        if (flag == -1)
            return index1;
        else if (flag == 1)
            return index2;

		return index1; // default
	}

	/**
     * Returns a permutation vector between the 0 and (length - 1)
     */
    private int[] intPermutation(int length)
    {
        int[] aux = new int[length];
        int[] result = new int[length];

        // first, create an array from 0 to length - 1
        // also is needed to create an random array of size length
        for (int i = 0; i < length; i++)
        {
            result[i] = i;
            aux[i] = Randomness.nextInt(0, length - 1);
        }

        // sort the random array with effect in result, and then we obtain a
        // permutation array between 0 and length - 1
        for (int i = 0; i < length; i++)
        {
            for (int j = i + 1; j < length; j++)
            {
                if (aux[i] > aux[j])
                {
                    int tmp;
                    tmp = aux[i];
                    aux[i] = aux[j];
                    aux[j] = tmp;
                    tmp = result[i];
                    result[i] = result[j];
                    result[j] = tmp;
                }
            }
        }

        return result;
    }
}
