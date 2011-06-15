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

import de.unisb.cs.st.evosuite.utils.Randomness;

/**
 * Cross individuals at identical point
 * 
 * @author Gordon Fraser
 * 
 */
public class SinglePointFixedCrossOver extends CrossOverFunction {

	private static final long serialVersionUID = 1215946828935020651L;

	/**
	 * The splitting point for to individuals p1, p2 is selected within
	 * min(length(p1),length(p2))
	 */
	@Override
	public void crossOver(Chromosome parent1, Chromosome parent2) throws ConstructionFailedException {

		int point = Randomness.nextInt(Math.min(parent1.size(), parent2.size()));

		Chromosome t1 = parent1.clone();
		Chromosome t2 = parent2.clone();

		parent1.crossOver(t2, point);
		parent2.crossOver(t1, point);
	}

}
