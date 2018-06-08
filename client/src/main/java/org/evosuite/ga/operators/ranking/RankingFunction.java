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
package org.evosuite.ga.operators.ranking;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;

/**
 * Interface for ranking algorithms
 * @author Annibale Panichella, Fitsum M. Kifetew
 *
 * @param <T>
 */
public interface RankingFunction<T extends Chromosome> extends Serializable {
	
	public void computeRankingAssignment(List<T> solutions, Set<FitnessFunction<T>> uncovered_goals);

	/**
	 * Returns a list of {@link org.evosuite.ga.Chromosome} objects of a given rank.
	 * 
	 * @param rank position
	 * @return a list of solutions of a given rank.
	 */
	public List<T> getSubfront(int rank);

	/**
	 * Returns the total number of sub-fronts founds.
	 */
	public int getNumberOfSubfronts();

}