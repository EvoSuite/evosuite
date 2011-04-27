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
 * Select individual by rank
 * 
 * @author Gordon Fraser
 * 
 */
public class RankSelection extends SelectionFunction {

	/** Bias towards better individuals */
	private final double rank_bias = Properties.getDoubleValue("rank_bias");

	@Override
	/**
	 * Select index of next offspring
	 * 
	 * Population has to be sorted!
	 */
	public int getIndex(List<Chromosome> population) {
		double r = randomness.nextDouble();
		double d = rank_bias
		        - Math.sqrt((rank_bias * rank_bias) - (4.0 * (rank_bias - 1.0) * r));
		int length = population.size();

		d = d / 2.0 / (rank_bias - 1.0);

		//this is not needed because population is sorted based on Maximization
		//if(maximize)
		//	d = 1.0 - d; // to do that if we want to have Maximisation

		int index = (int) (length * d);
		return index;
	}

}
