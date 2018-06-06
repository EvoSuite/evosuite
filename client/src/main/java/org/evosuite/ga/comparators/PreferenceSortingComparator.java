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
package org.evosuite.ga.comparators;

import java.io.Serializable;
import java.util.Comparator;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;

/**
 * This class implements a <code>Comparator</code> (a method for comparing <code>Chromosomes</code>
 * objects) based on the fitness value of two chromosome objects and considering the specified test
 * goals only.
 * 
 * @author Annibale Panichella
 */
public class PreferenceSortingComparator<T extends Chromosome> implements Comparator<Object>, Serializable {

	private static final long serialVersionUID = 8939172959105413213L;

	private final FitnessFunction<T> objective;

	/**
	 * Constructor
	 *
	 * @param goal a {@link org.evosuite.ga.FitnessFunction} object
	 */
	public PreferenceSortingComparator(FitnessFunction<T> goal) {
		this.objective = goal;
	}

	/**
	 * Compare the fitness value of two chromosome objects focusing only on one goal.
	 * 
	 * @param object1 a {@link org.evosuite.ga.Chromosome} object
	 * @param object2 a {@link org.evosuite.ga.Chromosome} object
	 * @return -1 if fitness value of object1 is lower than the fitness value of object2, 0 if the fitness
	 *         value of both objects is equal, or 1 if fitness value of object1 is higher than the fitness
	 *         value of object2.
	 */
	@SuppressWarnings("unchecked")
	public int compare(Object object1, Object object2) {
		if (object1 == null) {
			return 1;
		} else if (object2 == null) {
			return -1;
		}

		T solution1 = (T) object1;
		T solution2 = (T) object2;

		double value1, value2;
		value1 = solution1.getFitness(this.objective);
		value2 = solution2.getFitness(this.objective);
		if (value1 < value2) {
			return -1;
		} else if (value1 > value2) {
			return +1;
		} else {
			return solution1.compareSecondaryObjective(solution2);
		}
	}
}

