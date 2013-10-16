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

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;


/**
 * <p>LocalSearchObjective interface.</p>
 *
 * @author Gordon Fraser
 */
public interface LocalSearchObjective<T extends Chromosome> {

	/**
	 * <p>hasImproved</p>
	 *
	 * @param individual a {@link org.evosuite.ga.Chromosome} object.
	 * @return a boolean.
	 */
	public boolean hasImproved(T individual);

	/**
	 * <p>hasNotWorsened</p>
	 *
	 * @param individual a {@link org.evosuite.ga.Chromosome} object.
	 * @return a boolean.
	 */
	public boolean hasNotWorsened(T individual);

	/**
	 * <p>hasChanged</p>
	 *
	 * @param individual a {@link org.evosuite.ga.Chromosome} object.
	 * @return a int.
	 */
	public int hasChanged(T individual);

	/**
	 * Even if not successful, the local search might create some additional tests
	 * as a side effect which we may want to keep
	 * 
	 * @param individual
	 */
	public void retainPartialSolution(T individual);
	
	/**
	 * <p>getFitnessFunction</p>
	 *
	 * @return a {@link org.evosuite.ga.FitnessFunction} object.
	 */
	public FitnessFunction<? extends Chromosome> getFitnessFunction();

}
