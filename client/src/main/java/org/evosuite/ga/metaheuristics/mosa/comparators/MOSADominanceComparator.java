/**
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
package org.evosuite.ga.metaheuristics.mosa.comparators;

import java.util.Comparator;
import java.util.Set;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;

/**
 * This class implements a <code>Comparator</code> (a method for comparing <code>Chromosomes</code> objects) 
 * based on the dominance test and considering the specified test goals only.
 * 
 * @author Annibale Panichella
 */
public class MOSADominanceComparator<T extends Chromosome> implements Comparator<Object> {

	private Set<FitnessFunction<T>> objectives;

	/**
	 *  Constructor
	 * @param pNumberOfObjectives
	 * @param goals set of test goals to consider when computing the dominance relationship 
	 */
	public MOSADominanceComparator(Set<FitnessFunction<T>> goals) {
		this.objectives = goals;
	}

	/**
	 * Compares two test cases focusing only on the goals in {@link MOSADominanceComparator#objectives}".
	 * 
	 * @param object1
	 *            Object representing the first test cases.
	 * @param object2
	 *            Object representing the second test cases.
	 * @return -1, or 0, or 1 if object1 dominates object2, both are non-dominated, or solution1 is dominated by solution2, respectively.
	 */
	@SuppressWarnings("unchecked")
	public int compare(Object object1, Object object2) {
		if (object1 == null)
			return 1;
		else if (object2 == null)
			return -1;
		
		T solution1 = (T) object1;
		T solution2 = (T) object2;
		
		boolean dominate1 = false; // dominate1 indicates if some objective of solution1
								   // dominates the same objective in solution2. dominate2
		boolean dominate2 = false; // is the complementary of dominate1.

		double value1, value2;
		for (FitnessFunction<T> entry : objectives) {
			value1 = solution1.getFitness(entry);
			value2 = solution2.getFitness(entry); 
			if (value1 < value2)
                dominate1 = true;
            else if (value1 > value2)
                dominate2 = true;
            
            if (dominate1 && dominate2)
            	break;
        }

        if (dominate1 == dominate2)
            return 0; // no one dominates the other
        if (dominate1)
            return -1; // chromosome1 dominates

        return 1; // chromosome2 dominates
	} // compare
} // Dominance Comparator 

