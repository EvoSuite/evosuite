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
import java.util.LinkedHashMap;
import java.util.Map;

import org.evosuite.Properties;
import org.evosuite.ga.localsearch.LocalSearchObjective;
import org.evosuite.utils.PublicCloneable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Abstract base class of chromosomes
 * 
 * @author Gordon Fraser, Jose Miguel Rojas
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
	    // empty
	}

	/** Last recorded fitness value */
	private LinkedHashMap<FitnessFunction<?>, Double> fitnesses = new LinkedHashMap<FitnessFunction<?>, Double>();
	
	/** Previous fitness, to see if there was an improvement */
	private LinkedHashMap<FitnessFunction<?>, Double> lastFitnesses = new LinkedHashMap<FitnessFunction<?>, Double>();

	/** True if this is a solution */
	protected boolean solution = false;

	/** Has this chromosome changed since its fitness was last evaluated? */
	private boolean changed = true;

	protected double coverage = 0.0;

	protected int numOfCoveredGoals = 0;
	
	/** Generation in which this chromosome was created */
	protected int age = 0;

    /** */
    protected int rank = -1;

    /** */
    protected double distance = 0.0;

	/**
	 * Return current fitness value
	 * 
	 * @return a double.
	 */
	public double getFitness() {
		assert Properties.ALGORITHM != Properties.Algorithm.NSGAII;
		if (Properties.COMPOSITIONAL_FITNESS) {
            double sumFitnesses = 0.0;
            for (FitnessFunction<?> fitnessFunction : fitnesses.keySet()) {
                sumFitnesses += fitnesses.get(fitnessFunction);
            }
            return sumFitnesses;
        } else
            return fitnesses.isEmpty() ? 0.0 : fitnesses.get( fitnesses.keySet().iterator().next() );
	}

	public double getFitness(FitnessFunction<?> ff) {
        return fitnesses.get(ff);
    }

    public Map<FitnessFunction<?>, Double> getFitnesses() {
        return this.fitnesses;
    }

    public Map<FitnessFunction<?>, Double> getLastFitnesses() {
        return this.lastFitnesses;
    }

    public void setFitnesses(Map<FitnessFunction<?>, Double> fits) {
        this.fitnesses.clear();
        this.fitnesses.putAll(fits);
    }

    public void setLastFitnesses(Map<FitnessFunction<?>, Double> lastFits) {
        this.lastFitnesses.clear();
        this.lastFitnesses.putAll(lastFits);
    }

    public void addFitness(FitnessFunction<?> ff) {
        this.fitnesses.put(ff, 0.0);
        this.lastFitnesses.put(ff, 0.0);
    }

    public void addFitness(FitnessFunction<?> ff, double value) {
        this.fitnesses.put(ff, value);
        this.lastFitnesses.put(ff, value);
    }

	/**
	 * Set new fitness value
	 * 
	 * @param value
	 *            a double.
	 */
	public void setFitness(FitnessFunction<?> ff, double value) throws IllegalArgumentException{
		if ( (Double.compare(value, Double.NaN) == 0) ||
                (Double.isInfinite(value)) /*||
                (value < 0) ||
                (ff == null)*/ ) {
            throw new IllegalArgumentException("Invalid value of Fitness: " + value + ", Fitness: " + ff.getClass().getName());
        }

        if (!fitnesses.containsKey(ff)) {
            lastFitnesses.put(ff, value);
            fitnesses.put(ff, value);
        }
        else {
            lastFitnesses.put(ff, fitnesses.get(ff));
            fitnesses.put(ff, value);
        }
	}
	
	public boolean hasFitnessChanged() {
	    for (FitnessFunction<?> ff : fitnesses.keySet()) {
	        if (!fitnesses.get(ff).equals(lastFitnesses.get(ff))) {
	            return true;
	        }
	    }
	    return false;
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
	public int compareTo(Chromosome c) {
	    int i = (int) Math.signum(this.getFitness() - c.getFitness());
		if (i == 0)
			return compareSecondaryObjective(c);
		else
			return i;
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
	 *            a {@link org.evosuite.ga.localsearch.LocalSearchObjective} object.
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

    public int getRank() {
        return this.rank;
    }

    public void setRank(int r) {
        this.rank = r;
    }

    public double getDistance() {
        return this.distance;
    }

    public void setDistance(double d) {
        this.distance = d;
    }
}
