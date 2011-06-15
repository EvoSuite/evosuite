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
 * Decides when offspring replaces its parents for the next generation
 * 
 * @author Gordon Fraser
 *
 */
public abstract class ReplacementFunction {

	protected boolean maximize = false;
	
	public ReplacementFunction(SelectionFunction selection_function) {
		this.maximize = selection_function.maximize;
	}

	public ReplacementFunction(boolean maximize) {
		this.maximize = maximize;
	}

	protected boolean isBetter(Chromosome chromosome1, Chromosome chromosome2) {
		if(maximize) {
			return chromosome1.compareTo(chromosome2) > 0;
		} else {
			return chromosome1.compareTo(chromosome2) < 0;			
		}
		
	}
	
	protected Chromosome getBest(Chromosome chromosome1, Chromosome chromosome2) {
		if(isBetter(chromosome1, chromosome2))
			return chromosome1;
		else
			return chromosome2;
	}
	
	/**
	 * Decide whether to keep the offspring or the parents
	 * 
	 * @param parent1
	 * @param parent2
	 * @param offspring1
	 * @param offspring2
	 * @return
	 */
	public abstract boolean keepOffspring(Chromosome parent1, Chromosome parent2,
								 Chromosome offspring1, Chromosome offspring2);
	
	/**
	 * Decide which of two offspring to keep
	 * @param parent
	 * @param offspring
	 * @return
	 */
	public abstract boolean keepOffspring(Chromosome parent, Chromosome offspring);
}
