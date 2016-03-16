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
import java.util.LinkedHashMap;
import java.util.Map;

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
	private static final Logger logger = LoggerFactory.getLogger(Chromosome.class);

	/**
	 * only used for testing/debugging
	 */
	protected Chromosome() {
		// empty
	}
	protected boolean toBeUpdated=false;
	/** Last recorded fitness value */
	private LinkedHashMap<FitnessFunction<?>, Double> fitnessValues = new LinkedHashMap<FitnessFunction<?>, Double>();
	
	/** Previous fitness, to see if there was an improvement */
	private LinkedHashMap<FitnessFunction<?>, Double> previousFitnessValues = new LinkedHashMap<FitnessFunction<?>, Double>();

	/** Has this chromosome changed since its fitness was last evaluated? */
	private boolean changed = true;

	/** Has local search been applied to this individual since it was last changed? */
	private boolean localSearchApplied = false;

	private LinkedHashMap<FitnessFunction<?>, Double> coverageValues = new LinkedHashMap<FitnessFunction<?>, Double>();

	private LinkedHashMap<FitnessFunction<?>, Integer> numsNotCoveredGoals = new LinkedHashMap<FitnessFunction<?>, Integer>();

	private LinkedHashMap<FitnessFunction<?>, Integer> numsCoveredGoals = new LinkedHashMap<FitnessFunction<?>, Integer>();

	
	// protected double coverage = 0.0;

	// protected int numOfCoveredGoals = 0;

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
		if (fitnessValues.size() > 1) {
			double sumFitnesses = 0.0;
			for (FitnessFunction<?> fitnessFunction : fitnessValues.keySet()) {
				sumFitnesses += fitnessValues.get(fitnessFunction);
			}
			return sumFitnesses;
		} else
			return fitnessValues.isEmpty() ? 0.0 : fitnessValues.get(fitnessValues.keySet().iterator().next());
	}

	public <T extends Chromosome> double getFitness(FitnessFunction<T> ff) {
		return fitnessValues.containsKey(ff) ? fitnessValues.get(ff) : ff.getFitness((T)this); // Calculate new value if non is cached
	}

	public Map<FitnessFunction<?>, Double> getFitnessValues() {
		return this.fitnessValues;
	}

	public Map<FitnessFunction<?>, Double> getPreviousFitnessValues() {
		return this.previousFitnessValues;
	}
	
	public boolean hasExecutedFitness(FitnessFunction<?> ff) {
		return this.previousFitnessValues.containsKey(ff);
	}

	public void setFitnessValues(Map<FitnessFunction<?>, Double> fits) {
		//TODO mainfitness?
		this.fitnessValues.clear();
		this.fitnessValues.putAll(fits);
	}

	public void setPreviousFitnessValues(Map<FitnessFunction<?>, Double> lastFits) {
		this.previousFitnessValues.clear();
		this.previousFitnessValues.putAll(lastFits);
	}

	public boolean isToBeUpdated() {
		return toBeUpdated;
	}

	public void isToBeUpdated(boolean toBeUpdated) {
		this.toBeUpdated = toBeUpdated;
	}

	/**
	 * Adds a fitness function and sets fitness, coverage, and numCoveredGoal
	 * default.
	 *
	 * @param ff
	 *            a fitness function
	 */
	public void addFitness(FitnessFunction<?> ff) {
		if (ff.isMaximizationFunction())
			this.addFitness(ff, 0.0, 0.0, 0);
		else
			this.addFitness(ff, Double.MAX_VALUE, 0.0, 0);
	}

	/**
	 * Adds a fitness function with an associated fitness value
	 *
	 * @param ff
	 *            a fitness function
	 * @param fitnessValue
	 *            the fitness value for {@code ff}
	 */
	public void addFitness(FitnessFunction<?> ff, double fitnessValue) {
		this.addFitness(ff, fitnessValue, 0.0, 0);
	}

	/**
	 * Adds a fitness function with an associated fitness value and coverage
	 * value
	 *
	 * @param ff
	 *            a fitness function
	 * @param fitnessValue
	 *            the fitness value for {@code ff}
	 * @param coverage
	 *            the coverage value for {@code ff}
	 */
	public void addFitness(FitnessFunction<?> ff, double fitnessValue, double coverage) {
		this.fitnessValues.put(ff, fitnessValue);
		this.previousFitnessValues.put(ff, fitnessValue);
		this.coverageValues.put(ff, coverage);
		this.numsCoveredGoals.put(ff, 0);
		this.numsNotCoveredGoals.put(ff, -1);
	}

	/**
	 * Adds a fitness function with an associated fitness value, coverage value,
	 * and number of covered goals.
	 *
	 * @param ff
	 *            a fitness function
	 * @param fitnessValue
	 *            the fitness value for {@code ff}
	 * @param coverage
	 *            the coverage value for {@code ff}
	 * @param numCoveredGoals
	 *            the number of covered goals for {@code ff}
	 */
	public void addFitness(FitnessFunction<?> ff, double fitnessValue, double coverage,
			int numCoveredGoals) { 
		this.fitnessValues.put(ff, fitnessValue);
		this.previousFitnessValues.put(ff, fitnessValue);
		this.coverageValues.put(ff, coverage);
		this.numsCoveredGoals.put(ff, numCoveredGoals);
		this.numsNotCoveredGoals.put(ff, -1);
	}

	/**
	 * Set new fitness value
	 * 
	 * @param value
	 *            a double.
	 */
	public void setFitness(FitnessFunction<?> ff, double value) throws IllegalArgumentException {
		if ((Double.compare(value, Double.NaN) == 0) || (Double.isInfinite(value))) {
//				 || ( value < 0 ) || ( ff == null )) 
			throw new IllegalArgumentException("Invalid value of Fitness: " + value + ", Fitness: "
					+ ff.getClass().getName());
		}

		if (!fitnessValues.containsKey(ff)) {
			previousFitnessValues.put(ff, value);
			fitnessValues.put(ff, value);
		} else {
			previousFitnessValues.put(ff, fitnessValues.get(ff));
			fitnessValues.put(ff, value);
		}
	}

	public boolean hasFitnessChanged() {
		for (FitnessFunction<?> ff : fitnessValues.keySet()) {
			if (!fitnessValues.get(ff).equals(previousFitnessValues.get(ff))) {
				return true;
			}
		}
		return false;
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
		if (i == 0){
			return compareSecondaryObjective(c);
		}else
			return i;
	}

	/**
	 * Secondary Objectives are specific to chromosome types
	 * 
	 * @param o
	 *            a {@link org.evosuite.ga.Chromosome} object.
	 * @return a int.
	 */
	public abstract <T extends Chromosome> int compareSecondaryObjective(T o);

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
	public void crossOver(Chromosome other, int position) throws ConstructionFailedException {
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
	 *            a {@link org.evosuite.ga.localsearch.LocalSearchObjective}
	 *            object.
	 */
	public abstract boolean localSearch(LocalSearchObjective<? extends Chromosome> objective);

	/**
	 * Apply the local search
	 * 
	 * @param objective
	 *            a {@link org.evosuite.ga.LocalSearchObjective} object.
	 */
	// public void applyAdaptiveLocalSearch(LocalSearchObjective<? extends
	// Chromosome> objective) {
	// // No-op
	// }

	/**
	 * Apply DSE
	 * 
	 * @param algorithm
	 *            a {@link org.evosuite.ga.GeneticAlgorithm} object.
	 */
	// public abstract boolean applyDSE(GeneticAlgorithm<?> algorithm);

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
		// If it's changed, then that also implies LS is possible again
		localSearchApplied = false;
	}
	
	
	public boolean hasLocalSearchBeenApplied() {
		return localSearchApplied;
	}

	public void setLocalSearchApplied(boolean localSearchApplied) {
		this.localSearchApplied = localSearchApplied;
	}

	/**
	 * <p>
	 * Getter for the field <code>coverage</code>.
	 * </p>
	 *
	 * Returns a single coverage value if
	 * {@code Properties.COMPOSITIONAL_FITNESS} is {@code false}. Otherwise (
	 * {@code Properties.COMPOSITIONAL_FITNESS==true}), returns the average of
	 * coverage values for all fitness functions.
	 *
	 * @return a double.
	 */
	public double getCoverage() {
        double sum = 0;
        for (FitnessFunction<?> fitnessFunction : coverageValues.keySet()) {
            sum += coverageValues.get(fitnessFunction);
        }
        double cov = coverageValues.isEmpty() ? 0.0 : sum / coverageValues.size();
        assert (cov >= 0.0 && cov <= 1.0) : "Incorrect coverage value " + cov + ". Expected value between 0 and 1";
        return cov;
    }

	public int getNumOfCoveredGoals() {
        int sum = 0;
        for (FitnessFunction<?> fitnessFunction : numsCoveredGoals.keySet()) {
            sum += numsCoveredGoals.get(fitnessFunction);
        }
        return sum;
    }
	
	public int getNumOfNotCoveredGoals() {
        int sum = 0;
        for (FitnessFunction<?> fitnessFunction : numsNotCoveredGoals.keySet()) {
            sum += numsNotCoveredGoals.get(fitnessFunction);
        }
        return sum;
    }

	public void setNumsOfCoveredGoals(Map<FitnessFunction<?>, Integer> fits) {
		this.numsCoveredGoals.clear();
		this.numsCoveredGoals.putAll(fits);
	}

	public void setNumsOfNotCoveredGoals(Map<FitnessFunction<?>, Integer> fits) {
		this.numsNotCoveredGoals.clear();
		this.numsNotCoveredGoals.putAll(fits);
	}
	public void setNumOfNotCoveredGoals(FitnessFunction<?> ff, int numCoveredGoals) {
		this.numsNotCoveredGoals.put(ff, numCoveredGoals);
	}
	public Map<FitnessFunction<?>, Integer> getNumsOfCoveredGoals() {
		return this.numsCoveredGoals;
	}
	
	public LinkedHashMap<FitnessFunction<?>, Integer> getNumsNotCoveredGoals() {
		return numsNotCoveredGoals;
	}
	
	public Map<FitnessFunction<?>, Double> getCoverageValues() {
		return this.coverageValues;
	}

	public void setCoverageValues(Map<FitnessFunction<?>, Double> coverages) {
		this.coverageValues.clear();
		this.coverageValues.putAll(coverages);
	}

	// public void setNumOfCoveredGoals(int numOfCoveredGoals) {
	// this.numOfCoveredGoals = numOfCoveredGoals;
	// }

	/**
	 * Gets the coverage value for a given fitness function
	 *
	 * @param ff
	 *            a fitness function
	 * @return the number of covered goals for {@code ff}
	 */
	public double getCoverage(FitnessFunction<?> ff) {
		return coverageValues.containsKey(ff) ? coverageValues.get(ff) : 0.0;
	}

	/**
	 * Sets the coverage value for a given fitness function
	 *
	 * @param ff
	 *            a fitness function
	 * @param coverage
	 *            the coverage value
	 */
	public void setCoverage(FitnessFunction<?> ff, double coverage) {
		this.coverageValues.put(ff, coverage);
	}

	/**
	 * Gets the number of covered goals for a given fitness function
	 *
	 * @param ff
	 *            a fitness function
	 * @return the number of covered goals for {@code ff}
	 */
	public int getNumOfCoveredGoals(FitnessFunction<?> ff) {
		return numsCoveredGoals.containsKey(ff) ? numsCoveredGoals.get(ff) : 0;
	}
	
	/**
	 * Gets the number of not covered goals for a given fitness function
	 *
	 * @param ff
	 *            a fitness function
	 * @return the number of covered goals for {@code ff}
	 */
	public int getNumOfNotCoveredGoals(FitnessFunction<?> ff) {
		return numsNotCoveredGoals.containsKey(ff) ? numsNotCoveredGoals.get(ff) : 0;
	}

	/**
	 * Sets the number of covered goals for a given fitness function
	 *
	 * @param ff
	 *            a fitness function
	 * @param numCoveredGoals
	 *            the number of covered goals
	 */
	public void setNumOfCoveredGoals(FitnessFunction<?> ff, int numCoveredGoals) {
		this.numsCoveredGoals.put(ff, numCoveredGoals);
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

	public double getFitnessInstanceOf(Class<?> clazz) {
		for (FitnessFunction<?> fitnessFunction : fitnessValues.keySet()) {
			if (clazz.isInstance(fitnessFunction))
				return fitnessValues.get(fitnessFunction);
		}
		return 0.0;
	}

	public double getCoverageInstanceOf(Class<?> clazz) {
		for (FitnessFunction<?> fitnessFunction : coverageValues.keySet()) {
			if (clazz.isInstance(fitnessFunction))
				return coverageValues.get(fitnessFunction);
		}
		return 0.0;
	}
}
