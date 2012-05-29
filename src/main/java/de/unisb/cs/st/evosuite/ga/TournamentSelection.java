/**
 * Copyright (C) 2012 Gordon Fraser, Andrea Arcuri
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package de.unisb.cs.st.evosuite.ga;

import java.util.List;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.utils.Randomness;

/**
 * Select an individual from a population as winner of a number of tournaments
 * 
 * @author Gordon Fraser
 * 
 */
public class TournamentSelection extends SelectionFunction {

	private static final long serialVersionUID = -7465418404056357932L;

	/**
	 * Perform the tournament on the population, return one index
	 */
	@Override
	public int getIndex(List<Chromosome> population) {
		int new_num = Randomness.nextInt(population.size());
		int winner = new_num;

		int round = 0;

		while (round < Properties.TOURNAMENT_SIZE - 1) {
			new_num = Randomness.nextInt(population.size());
			Chromosome selected = population.get(new_num);

			if (maximize) {
				if (selected.getFitness() > population.get(winner).getFitness()) {
					winner = new_num;
				}
			} else {
				if (selected.getFitness() < population.get(winner).getFitness()) {
					winner = new_num;
				}
			}
			round++;
		}

		return winner;
	}

}
