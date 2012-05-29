/**
 * Copyright (C) 2012 Gordon Fraser, Andrea Arcuri
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.utils.PublicCloneable;

/**
 * Abstract base class of chromosomes
 * 
 * @author Gordon Fraser
 * 
 */
public abstract class Chromosome implements Comparable<Chromosome>, Serializable,
        PublicCloneable<Chromosome> {

	private static final long serialVersionUID = -6921897301005213358L;

	protected static Logger logger = LoggerFactory.getLogger(Chromosome.class);

	/**
	 * only used for testing/debugging
	 */
	protected Chromosome(){
		
	}
	
	/** Last recorded fitness value */
	private double fitness = 0.0;

	/** True if this is a solution */
	protected boolean solution = false;

	/** Has this chromosome changed since its fitness was last evaluated? */
	protected boolean changed = true;

	/**
	 * Return current fitness value
	 * 
	 * @return
	 */
	public double getFitness() {
		return fitness;
	}

	/**
	 * Set new fitness value
	 * 
	 * @param value
	 */
	public void setFitness(double value) {
		fitness = value;
		// changed = false;
	}

	/**
	 * Is this a valid solution?
	 * 
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
	@Override
	public abstract Chromosome clone();

	@Override
	public abstract boolean equals(Object obj);

	@Override
	public abstract int hashCode();

	/**
	 * Determine relative ordering of this chromosome to another chromosome. If
	 * the fitness values are equal, go through all secondary objectives and try
	 * to find one where the two are not equal.
	 */
	@Override
	public int compareTo(Chromosome o) {
		int c = (int) Math.signum(fitness - o.fitness);
		if (c == 0)
			return compareSecondaryObjective(o);
		else
			return c;

	}

	/**
	 * Secondary Objectives are specific to chromosome types
	 * 
	 * @param o
	 * @return
	 */
	public abstract int compareSecondaryObjective(Chromosome o);

	/**
	 * Apply mutation
	 */
	public abstract void mutate();

	/**
	 * Fixed single point cross over
	 * 
	 * @param other
	 * @param position
	 * @throws ConstructionFailedException
	 */
	public void crossOver(Chromosome other, int position)
	        throws ConstructionFailedException {
		crossOver(other, position, position);
	}

	/**
	 * Single point cross over
	 * 
	 * @param other
	 * @param position1
	 * @param position2
	 * @throws ConstructionFailedException
	 */
	public abstract void crossOver(Chromosome other, int position1, int position2)
	        throws ConstructionFailedException;

	/**
	 * Apply the local search
	 */
	public abstract void localSearch(LocalSearchObjective objective);

	/**
	 * Apply DSE
	 */
	public abstract void applyDSE(GeneticAlgorithm algorithm);

	/**
	 * Return length of individual
	 * 
	 * @return
	 */
	public abstract int size();

	/**
	 * Return whether the chromosome has changed since the fitness value was
	 * computed last
	 * 
	 * @return
	 */
	public boolean isChanged() {
		return changed;
	}

	/**
	 * Set changed status to @param changed
	 * 
	 * @param changed
	 */
	public void setChanged(boolean changed) {
		this.changed = changed;
	}
}
