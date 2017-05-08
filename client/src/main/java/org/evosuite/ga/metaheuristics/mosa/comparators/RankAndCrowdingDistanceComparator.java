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

import org.evosuite.ga.Chromosome;

/**
 * This class implements a <code>Comparator</code> (a method for comparing <code>Chromosomes</code> 
 * objects) based on the dominance test, as in NSGA-II.
 * 
 * @author Annibale Panichella
 */
public class RankAndCrowdingDistanceComparator<T extends Chromosome> implements Comparator<Object> {

	/**
	 * Constructor
	 * 
	 * @param comparator
	 */
	public RankAndCrowdingDistanceComparator() {

	}

	/**
	 * Compares two solutions.
	 * 
	 * @param object1
	 *            Object representing the first <code>Solution</code>.
	 * @param object2
	 *            Object representing the second <code>Solution</code>.
	 * @return -1, or 0, or 1 according to the non-dominated ranks
	 */
	@SuppressWarnings("unchecked")
	public int compare(Object object1, Object object2) {
		if (object1 == null)
			return 1;
		else if (object2 == null)
			return -1;

		T solution1 = (T) object1;
		T solution2 = (T) object2;

		if (solution1.getRank()<solution2.getRank())
			return -1;
		else if (solution1.getRank()>solution2.getRank())
			return +1;
		else if (solution1.getDistance()>solution2.getDistance())
			return -1;
		else if (solution2.getDistance()>solution1.getDistance())
			return +1;
		return 0;
	} // compare
} // DominanceComparator

