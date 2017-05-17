/**
 * Copyright (C) 2010-2017 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.utils;

/**
 * Implementation of binomial distribution based on
 * http://stackoverflow.com/a/1241605/998816
 * 
 * @author Yan Ge
 */
public class BinomialDistribution {

	public static int sample(int testSize, double mutationProbability) {
		// The number of selected bits used for mutation operator
		int numberSample = 0;
		for (int i = 0; i < testSize; i++) {
			if (Randomness.nextDouble() < mutationProbability) {
				numberSample++;
			}
		}
		return numberSample;
	}
}
