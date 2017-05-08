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
package org.evosuite.ga.metaheuristics.mosa;

import java.util.Comparator;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.metaheuristics.mosa.comparators.RankAndCrowdingDistanceComparator;
import org.evosuite.ga.operators.selection.SelectionFunction;
import org.evosuite.utils.Randomness;


/**
 * Select an individual from a population as winner of a number of tournaments according
 * to the "non-dominance" relationship and the crowding distance
 *
 * @author Annibale Panichella, Fitsum M. Kifetew
 */
public class MOSATournamentSelection<T extends Chromosome> extends SelectionFunction<T> {

	private static final long serialVersionUID = -7465418404056357932L;

	private Comparator<Object> comparator = new RankAndCrowdingDistanceComparator<T>();

	/**
	 * {@inheritDoc}
	 *
	 * Perform the tournament on the population, return one index
	 */
	@Override
	public int getIndex(List<T> population) {
		int new_num = Randomness.nextInt(population.size());
		int winner = new_num;

		int round = 0;

		while (round < Properties.TOURNAMENT_SIZE - 1) {
			new_num = Randomness.nextInt(population.size());
			if (new_num == winner)
				new_num = (new_num+1) % population.size();
			Chromosome selected = population.get(new_num);
			int flag = comparator.compare(selected, population.get(winner));
			if (flag==-1) {
				winner = new_num;
			} 
			round++;
		}

		return winner;
	}

	@Override
	public T select(List<T> population) {
		return population.get(getIndex(population));
	}
}
