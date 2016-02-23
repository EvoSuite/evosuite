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

/**
 * Decides when offspring replaces its parents for the next generation
 * 
 * @author Gordon Fraser
 */
public abstract class ReplacementFunction implements Serializable {

	private static final long serialVersionUID = 8507488475265387482L;

	protected boolean maximize = false;

	/**
	 * <p>
	 * Constructor for ReplacementFunction.
	 * </p>
	 * 
	 * @param maximize
	 *            a boolean.
	 */
	public ReplacementFunction(boolean maximize) {
		this.maximize = maximize;
	}

	/**
	 * <p>
	 * isBetter
	 * </p>
	 * 
	 * @param chromosome1
	 *            a {@link org.evosuite.ga.Chromosome} object.
	 * @param chromosome2
	 *            a {@link org.evosuite.ga.Chromosome} object.
	 * @return a boolean.
	 */
	protected boolean isBetter(Chromosome chromosome1, Chromosome chromosome2) {
		if (maximize) {
			return chromosome1.compareTo(chromosome2) > 0;
		} else {
			return chromosome1.compareTo(chromosome2) < 0;
		}
	}

	/**
	 * <p>
	 * isBetterOrEqual
	 * </p>
	 * 
	 * @param chromosome1
	 *            a {@link org.evosuite.ga.Chromosome} object.
	 * @param chromosome2
	 *            a {@link org.evosuite.ga.Chromosome} object.
	 * @return a boolean.
	 */
	protected boolean isBetterOrEqual(Chromosome chromosome1, Chromosome chromosome2) {
		if (maximize) {
			return chromosome1.compareTo(chromosome2) >= 0;
		} else {
			return chromosome1.compareTo(chromosome2) <= 0;
		}
	}

	/**
	 * <p>
	 * getBest
	 * </p>
	 * 
	 * @param chromosome1
	 *            a {@link org.evosuite.ga.Chromosome} object.
	 * @param chromosome2
	 *            a {@link org.evosuite.ga.Chromosome} object.
	 * @return a {@link org.evosuite.ga.Chromosome} object.
	 */
	protected Chromosome getBest(Chromosome chromosome1, Chromosome chromosome2) {
		if (isBetter(chromosome1, chromosome2))
			return chromosome1;
		else
			return chromosome2;
	}

	/**
	 * Decide whether to keep the offspring or the parents
	 * 
	 * @param parent1
	 *            a {@link org.evosuite.ga.Chromosome} object.
	 * @param parent2
	 *            a {@link org.evosuite.ga.Chromosome} object.
	 * @param offspring1
	 *            a {@link org.evosuite.ga.Chromosome} object.
	 * @param offspring2
	 *            a {@link org.evosuite.ga.Chromosome} object.
	 * @return a boolean.
	 */
	public boolean keepOffspring(Chromosome parent1, Chromosome parent2,
	        Chromosome offspring1, Chromosome offspring2) {
		if (maximize) {
			return compareBestOffspringToBestParent(parent1, parent2, offspring1,
			                                        offspring2) >= 0;
		} else {
			return compareBestOffspringToBestParent(parent1, parent2, offspring1,
			                                        offspring2) <= 0;
		}
	}

	/**
	 * Check how the best offspring compares with best parent
	 * 
	 * @param parent1
	 *            a {@link org.evosuite.ga.Chromosome} object.
	 * @param parent2
	 *            a {@link org.evosuite.ga.Chromosome} object.
	 * @param offspring1
	 *            a {@link org.evosuite.ga.Chromosome} object.
	 * @param offspring2
	 *            a {@link org.evosuite.ga.Chromosome} object.
	 * @return a int.
	 */
	protected int compareBestOffspringToBestParent(Chromosome parent1,
	        Chromosome parent2, Chromosome offspring1, Chromosome offspring2) {

		Chromosome bestOffspring = getBest(offspring1, offspring2);
		Chromosome bestParent = getBest(parent1, parent2);

		return bestOffspring.compareTo(bestParent);
	}

	/**
	 * Decide which of two offspring to keep
	 * 
	 * @param parent
	 *            a {@link org.evosuite.ga.Chromosome} object.
	 * @param offspring
	 *            a {@link org.evosuite.ga.Chromosome} object.
	 * @deprecated should not be used, as it does not handle
	 *             Properties.CHECK_PARENTS_LENGTH
	 * @return a boolean.
	 */
	@Deprecated
	public boolean keepOffspring(Chromosome parent, Chromosome offspring) {
		return isBetterOrEqual(offspring, parent);
	}
}
