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

import org.apache.log4j.Logger;

/**
 * Abstract base class of chromosomes
 * 
 * @author Gordon Fraser
 *
 */
public abstract class Chromosome implements Comparable<Chromosome> {

	protected static Logger logger = Logger.getLogger(Chromosome.class);

	/**
	 * No GA without randomnes
	 */
	protected Randomness randomness = Randomness.getInstance();
	
	/**
	 * Exception to handle the case when a mutation fails
	 *
	 */
	class MutationFailedException extends Exception {
		private static final long serialVersionUID = 1667810363133452317L;
	};
	
	/**
	 * Last recorded fitness value
	 */
	private double fitness = 0.0;
	
	/** True if this is a solution */
	protected boolean solution = false;
	
	/** Has this chromosome changed since its fitness was last evaluated? */
	private boolean changed = true;
	
	/**
	 * Return current fitness value
	 * @return
	 */
	public double getFitness() {
		return fitness;
	}
	
	/**
	 * Set new fitness value
	 * @param value
	 */
	public void setFitness(double value) {
		fitness = value;
		//changed = false;
	}
	
	/**
	 * Is this a valid solution?
	 * @return
	 */
	public boolean isSolution() {
		return solution;
	}
	
	public void setSolution(boolean value) {
		solution = value;
	}
	
	/**
	 * Create a deep copy of the chromosome
	 */
	public abstract Chromosome clone();
	
	@Override
	public abstract boolean equals(Object obj);

	/**
	 * Determine relative ordering of this chromosome to another chromosome
	 */
	public int compareTo(Chromosome o) {
		return (int) Math.signum(fitness - o.fitness);
	}
	
	/**
	 * Apply mutation
	 */
	public abstract void mutate();
	
	/**
	 * Fixed single point cross over
	 * @param other
	 * @param position
	 * @throws ConstructionFailedException 
	 */
	public void crossOver(Chromosome other, int position) throws ConstructionFailedException {
		crossOver(other, position, position);
	}

	/**
	 * Single point cross over
	 * @param other
	 * @param position1
	 * @param position2
	 * @throws ConstructionFailedException 
	 */
	public abstract void crossOver(Chromosome other, int position1, int position2) throws ConstructionFailedException;

	/**
	 * Return length of individual
	 * @return
	 */
	public abstract int size();

	public boolean isChanged() {
		return changed;
	}
	
	public void setChanged(boolean changed) {
		this.changed = changed;
	}
}
