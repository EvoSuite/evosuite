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
package org.evosuite.ga;

import java.io.Serializable;

import org.evosuite.localsearch.LocalSearchObjective;
import org.evosuite.utils.PublicCloneable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class of chromosomes
 * 
 * @author Gordon Fraser
 */
public abstract class Chromosome implements Comparable<Chromosome>, Serializable,
        PublicCloneable<Chromosome> {

	private static final long serialVersionUID = -6921897301005213358L;

	/** Constant <code>logger</code> */
	protected static final Logger logger = LoggerFactory.getLogger(Chromosome.class);

	/**
	 * only used for testing/debugging
	 */
	protected Chromosome() {

	}

	/** Last recorded fitness value */
	private double fitness = 0.0;
	
	/** Previous fitness, to see if there was an improvement */
	private double lastFitness = 0.0;

	/** True if this is a solution */
	protected boolean solution = false;

	/** Has this chromosome changed since its fitness was last evaluated? */
	private boolean changed = true;

	protected double coverage = 0.0;

	protected int numOfCoveredGoals = 0;
	
	/** Generation in which this chromosome was created */
	protected int age = 0;
	
	/**
	 * Return current fitness value
	 * 
	 * @return a double.
	 */
	public double getFitness() {
		return fitness;
	}

	/**
	 * Set new fitness value
	 * 
	 * @param value
	 *            a double.
	 */
	public void setFitness(double value) throws IllegalArgumentException{
		if(value < 0){
			throw new IllegalArgumentException("Fitness can never be negative: "+value);
		}
		lastFitness = fitness;
		fitness = value;
		// changed = false;
	}
	
	public boolean hasFitnessChanged() {
		return fitness != lastFitness;
	}

	/**
	 * Is this a valid solution?
	 * 
	 * @return a boolean.
	 */
	public boolean isSolution() {
		return solution;
	}

	/**
	 * <p>
	 * Setter for the field <code>solution</code>.
	 * </p>
	 * 
	 * @param value
	 *            a boolean.
	 */
	public void setSolution(boolean value) {
		solution = value;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Create a deep copy of the chromosome
	 */
	@Override
	public abstract Chromosome clone();

	/** {@inheritDoc} */
	@Override
	public abstract boolean equals(Object obj);

	/** {@inheritDoc} */
	@Override
	public abstract int hashCode();

	/**
	 * {@inheritDoc}
	 * 
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
	 *            a {@link org.evosuite.ga.Chromosome} object.
	 * @return a int.
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
	 *            a {@link org.evosuite.ga.Chromosome} object.
	 * @param position
	 *            a int.
	 * @throws org.evosuite.ga.ConstructionFailedException
	 *             if any.
	 */
	public void crossOver(Chromosome other, int position)
	        throws ConstructionFailedException {
		crossOver(other, position, position);
	}

	/**
	 * Single point cross over
	 * 
	 * @param other
	 *            a {@link org.evosuite.ga.Chromosome} object.
	 * @param position1
	 *            a int.
	 * @param position2
	 *            a int.
	 * @throws org.evosuite.ga.ConstructionFailedException
	 *             if any.
	 */
	public abstract void crossOver(Chromosome other, int position1, int position2)
	        throws ConstructionFailedException;

	/**
	 * Apply the local search
	 * 
	 * @param objective
	 *            a {@link org.evosuite.localsearch.LocalSearchObjective} object.
	 */
	public abstract boolean localSearch(LocalSearchObjective<? extends Chromosome> objective);

	/**
	 * Apply the local search
	 * 
	 * @param objective
	 *            a {@link org.evosuite.ga.LocalSearchObjective} object.
	 */
	//public void applyAdaptiveLocalSearch(LocalSearchObjective<? extends Chromosome> objective) {
	//	// No-op
	//}

	/**
	 * Apply DSE
	 * 
	 * @param algorithm
	 *            a {@link org.evosuite.ga.GeneticAlgorithm} object.
	 */
	//public abstract boolean applyDSE(GeneticAlgorithm<?> algorithm);

	/**
	 * Return length of individual
	 * 
	 * @return a int.
	 */
	public abstract int size();

	/**
	 * Return whether the chromosome has changed since the fitness value was
	 * computed last
	 * 
	 * @return a boolean.
	 */
	public boolean isChanged() {
		return changed;
	}

	/**
	 * Set changed status to @param changed
	 * 
	 * @param changed
	 *            a boolean.
	 */
	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	/**
	 * <p>Getter for the field <code>coverage</code>.</p>
	 *
	 * @return a double.
	 */
	public double getCoverage() {
		return coverage;
	}

	/**
	 * <p>Setter for the field <code>coverage</code>.</p>
	 *
	 * @param coverage a double.
	 */
	public void setCoverage(double coverage) {
		this.coverage = coverage;
	}

	public int getNumOfCoveredGoals() {
		return numOfCoveredGoals;
	}

	public void setNumOfCoveredGoals(int numOfCoveredGoals) {
		this.numOfCoveredGoals = numOfCoveredGoals;
	}
	
	public void updateAge(int generation) {
		this.age = generation;
	}
	
	public int getAge() {
		return age;
	}
}
