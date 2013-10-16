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
package org.evosuite.localsearch;

import java.io.Serializable;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;

/**
 * <p>DefaultLocalSearchObjective class.</p>
 *
 * @author Gordon Fraser
 */
public class DefaultLocalSearchObjective<T extends Chromosome> implements LocalSearchObjective<T>, Serializable {

	private static final long serialVersionUID = -8640106627078837108L;

	private final FitnessFunction<? extends Chromosome> fitness;

	/**
	 * <p>Constructor for DefaultLocalSearchObjective.</p>
	 *
	 * @param fitness a {@link org.evosuite.ga.FitnessFunction} object.
	 */
	public DefaultLocalSearchObjective(FitnessFunction<? extends Chromosome> fitness) {
		this.fitness = fitness;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.LocalSearchObjective#hasImproved(org.evosuite.ga.Chromosome)
	 */
	/** {@inheritDoc} */
	@Override
	public boolean hasImproved(T individual) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.LocalSearchObjective#getFitnessFunction()
	 */
	/** {@inheritDoc} */
	@Override
	public FitnessFunction<? extends Chromosome> getFitnessFunction() {
		return fitness;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.LocalSearchObjective#hasChanged(org.evosuite.ga.Chromosome)
	 */
	/** {@inheritDoc} */
	@Override
	public int hasChanged(T individual) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.LocalSearchObjective#hasNotWorsened(org.evosuite.ga.Chromosome)
	 */
	/** {@inheritDoc} */
	@Override
	public boolean hasNotWorsened(T individual) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void retainPartialSolution(T individual) {
		// Ignore		
	}

}
