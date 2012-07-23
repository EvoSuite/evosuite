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
package org.evosuite.ga;

/**
 * <p>LocalSearchObjective interface.</p>
 *
 * @author Gordon Fraser
 */
public interface LocalSearchObjective {

	/**
	 * <p>hasImproved</p>
	 *
	 * @param individual a {@link org.evosuite.ga.Chromosome} object.
	 * @return a boolean.
	 */
	public boolean hasImproved(Chromosome individual);

	/**
	 * <p>hasNotWorsened</p>
	 *
	 * @param individual a {@link org.evosuite.ga.Chromosome} object.
	 * @return a boolean.
	 */
	public boolean hasNotWorsened(Chromosome individual);

	/**
	 * <p>hasChanged</p>
	 *
	 * @param individual a {@link org.evosuite.ga.Chromosome} object.
	 * @return a int.
	 */
	public int hasChanged(Chromosome individual);

	/**
	 * <p>getFitnessFunction</p>
	 *
	 * @return a {@link org.evosuite.ga.FitnessFunction} object.
	 */
	public FitnessFunction getFitnessFunction();

}
