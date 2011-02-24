/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of the GA library.
 * 
 * GA is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * GA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with
 * GA. If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.ga;

import java.util.List;

import de.unisb.cs.st.evosuite.Properties;

/**
 * Select an individual from a population as winner of a number of tournaments
 * 
 * @author Gordon Fraser
 * 
 */
public class TournamentSelection extends SelectionFunction {

	/** Number of tournaments to run */
	private final int tournament_size = Properties.getPropertyOrDefault("tournament_size",
	                                                                    5);

	/**
	 * Perform the tournament on the population, return one index
	 */
	@Override
	public int getIndex(List<Chromosome> population) {
		int new_num = randomness.nextInt(population.size());
		int winner = new_num;

		int round = 0;

		while (round < tournament_size) {
			new_num = randomness.nextInt(population.size());
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
