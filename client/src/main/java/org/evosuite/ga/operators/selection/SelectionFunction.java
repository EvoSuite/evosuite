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
package org.evosuite.ga.operators.selection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.evosuite.ga.Chromosome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class of selection functions
 * 
 * @author Gordon Fraser
 */
public abstract class SelectionFunction<T extends Chromosome> implements Serializable {

	private static final long serialVersionUID = -2514933149542277609L;

	/** Constant <code>logger</code> */
	protected static final Logger logger = LoggerFactory.getLogger(SelectionFunction.class);

	/**
	 * Do we want to minimize or maximize fitness?
	 */
	protected boolean maximize = true;

	/**
	 * Return index of next offspring
	 * 
	 * @param population
	 *            a {@link java.util.List} object.
	 * @return a int.
	 */
	public abstract int getIndex(List<T> population);

	/**
	 * Return two parents
	 * 
	 * @param population
	 *            a {@link java.util.List} object.
	 * @return a {@link org.evosuite.ga.Chromosome} object.
	 */
	public T select(List<T> population) {
		return select(population, 1).get(0);
	}

	/**
	 * Return n parents
	 * 
	 * @param population
	 *            a {@link java.util.List} object.
	 * @param number
	 *            n
	 * @return a {@link java.util.List} object.
	 */
	public List<T> select(List<T> population, int number) {
		List<T> offspring = new ArrayList<T>();
		for (int i = 0; i < number; i++) {
			offspring.add(population.get(getIndex(population)));
		}
		return offspring;
	}

	/**
	 * Are we maximizing or minimizing fitness?
	 * 
	 * @param max
	 *            a boolean.
	 */
	public void setMaximize(boolean max) {
		maximize = max;
	}

	/**
	 * <p>
	 * isMaximize
	 * </p>
	 * 
	 * @return true is we have to maximize
	 */
	public boolean isMaximize() {
		return maximize;
	}

}
