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

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.utils.Randomness;


	/**
	 * {@inheritDoc}
	 *
	 * Select individual by rank
	 */
public class RankSelection<T extends Chromosome> extends SelectionFunction<T> {

	private static final long serialVersionUID = 7849303009915557682L;
	@Override
	/**
	 * Select index of next offspring
	 * 
	 * Population has to be sorted!
	 */
	public int getIndex(List<T> population) {
		double r = Randomness.nextDouble();
		double d = Properties.RANK_BIAS
		        - Math.sqrt((Properties.RANK_BIAS * Properties.RANK_BIAS)
		                - (4.0 * (Properties.RANK_BIAS - 1.0) * r));
		int length = population.size();

		d = d / 2.0 / (Properties.RANK_BIAS - 1.0);

		//this is not needed because population is sorted based on Maximization
		//if(maximize)
		//	d = 1.0 - d; // to do that if we want to have Maximisation

		int index = (int) (length * d);
		return index;
	}

}
