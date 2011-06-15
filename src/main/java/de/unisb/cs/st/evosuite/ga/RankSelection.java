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
import de.unisb.cs.st.evosuite.utils.Randomness;

/**
 * Select individual by rank
 * 
 * @author Gordon Fraser
 * 
 */
public class RankSelection extends SelectionFunction {

	private static final long serialVersionUID = 7849303009915557682L;

	@Override
	/**
	 * Select index of next offspring
	 * 
	 * Population has to be sorted!
	 */
	public int getIndex(List<Chromosome> population) {
		double r = Randomness.nextDouble();
		double d = Properties.RANK_BIAS
				- Math.sqrt((Properties.RANK_BIAS * Properties.RANK_BIAS) - (4.0 * (Properties.RANK_BIAS - 1.0) * r));
		int length = population.size();

		d = d / 2.0 / (Properties.RANK_BIAS - 1.0);

		// this is not needed because population is sorted based on Maximization
		// if(maximize)
		// d = 1.0 - d; // to do that if we want to have Maximisation

		int index = (int) (length * d);
		return index;
	}

}
