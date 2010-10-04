/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of the GA library.
 * 
 * GA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * GA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License
 * along with GA.  If not, see <http://www.gnu.org/licenses/>.
 */


package de.unisb.cs.st.evosuite.ga;

/**
 * Select one random point in each individual and cross over (TPX)
 * @author Gordon Fraser
 *
 */
public class SinglePointCrossOver extends CrossOverFunction {

	/**
	 * A different splitting point is selected for each individual
	 */
	@Override
	public void crossOver(Chromosome parent1, Chromosome parent2) throws ConstructionFailedException {

		if(parent1.size() < 2 || parent2.size() < 2) {
			return;
		}
		// Choose a position in the middle
		int point1 = randomness.nextInt(parent1.size() - 1) + 1;
		int point2 = randomness.nextInt(parent2.size() - 1) + 1;

		Chromosome t1 = parent1.clone();
		Chromosome t2 = parent2.clone();
		
		parent1.crossOver(t2, point1, point2);
		parent2.crossOver(t1, point2, point1);		
	}

}
