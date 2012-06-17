/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package de.unisb.cs.st.evosuite.ga;

import java.io.Serializable;

/**
 * @author Gordon Fraser
 * 
 */
public class DefaultLocalSearchObjective implements LocalSearchObjective, Serializable {

	private static final long serialVersionUID = -8640106627078837108L;

	private final FitnessFunction fitness;

	public DefaultLocalSearchObjective(FitnessFunction fitness) {
		this.fitness = fitness;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.LocalSearchObjective#hasImproved(de.unisb.cs.st.evosuite.ga.Chromosome)
	 */
	@Override
	public boolean hasImproved(Chromosome individual) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.LocalSearchObjective#getFitnessFunction()
	 */
	@Override
	public FitnessFunction getFitnessFunction() {
		return fitness;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.LocalSearchObjective#hasChanged(de.unisb.cs.st.evosuite.ga.Chromosome)
	 */
	@Override
	public int hasChanged(Chromosome individual) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.LocalSearchObjective#hasNotWorsened(de.unisb.cs.st.evosuite.ga.Chromosome)
	 */
	@Override
	public boolean hasNotWorsened(Chromosome individual) {
		// TODO Auto-generated method stub
		return false;
	}

}
