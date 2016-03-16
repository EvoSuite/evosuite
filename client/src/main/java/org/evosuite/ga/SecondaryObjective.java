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
package org.evosuite.ga;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Abstract SecondaryObjective class.
 * </p>
 * 
 * @author Gordon Fraser
 */
public abstract class SecondaryObjective<T extends Chromosome> implements Serializable {

	private static final long serialVersionUID = -4117187516650844086L;

	/** Constant <code>logger</code> */
	protected static final Logger logger = LoggerFactory.getLogger(SecondaryObjective.class);

	/**
	 * Compare two chromosomes with each other with respect to this objective.
	 * This comparison is mainly used during ranking.
	 * 
	 * @param chromosome1
	 *            a {@link org.evosuite.ga.Chromosome} object.
	 * @param chromosome2
	 *            a {@link org.evosuite.ga.Chromosome} object.
	 * @return a int.
	 */
	public abstract int compareChromosomes(T chromosome1, T chromosome2);

	/**
	 * Compare two parents to decide whether the children can replace the
	 * parents.
	 * 
	 * @param parent1
	 *            a {@link org.evosuite.ga.Chromosome} object.
	 * @param parent2
	 *            a {@link org.evosuite.ga.Chromosome} object.
	 * @param child1
	 *            a {@link org.evosuite.ga.Chromosome} object.
	 * @param child2
	 *            a {@link org.evosuite.ga.Chromosome} object.
	 * @return a int.
	 */
	public abstract int compareGenerations(T parent1, T parent2,
	        T child1, T child2);

}
