/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package de.unisb.cs.st.evosuite.ga;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class of selection functions
 * 
 * @author Gordon Fraser
 * 
 */
public abstract class SelectionFunction implements Serializable {

	private static final long serialVersionUID = -2514933149542277609L;

	protected static Logger logger = LoggerFactory.getLogger(SelectionFunction.class);

	/**
	 * Do we want to minimize or maximize fitness?
	 */
	protected boolean maximize = true;

	/**
	 * Return index of next offspring
	 * 
	 * @param population
	 * @return
	 */
	public abstract int getIndex(List<Chromosome> population);

	/**
	 * Return two parents
	 * 
	 * @param population
	 * @return
	 */
	public Chromosome select(List<Chromosome> population) {
		return select(population, 1).get(0);
	}

	/**
	 * Return n parents
	 * 
	 * @param population
	 * @param number
	 *            n
	 * @return
	 */
	public List<Chromosome> select(List<Chromosome> population, int number) {
		List<Chromosome> offspring = new ArrayList<Chromosome>();
		for (int i = 0; i < number; i++) {
			offspring.add(population.get(getIndex(population)));
		}
		return offspring;
	}

	/**
	 * Are we maximizing or minimizing fitness?
	 * 
	 * @param max
	 */
	public void setMaximize(boolean max) {
		maximize = max;
	}

	/**
	 * 
	 * @return true is we have to maximize
	 */
	public boolean isMaximize() {
		return maximize;
	}

}
