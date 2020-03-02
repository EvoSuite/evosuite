/**
 * Copyright (C) 2010-2020 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.symbolic.DSE.algorithm;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.stoppingconditions.StoppingCondition;
import org.evosuite.symbolic.DSE.algorithm.listener.SymbolicExecutionSearchListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Abstract superclass of DSE algorithms
 *
 * @author Ignacio Lebrero
 */
public abstract class DSEBaseAlgorithm<T extends Chromosome> {

	/** Listeners */
	protected transient Set<SymbolicExecutionSearchListener> listeners = new HashSet<SymbolicExecutionSearchListener>();

	/** Fitness Functions */
	protected transient List<FitnessFunction<T>> fitnessFunctions = new ArrayList<FitnessFunction<T>>();

	/** List of conditions on which to end the search */
	protected transient Set<StoppingCondition> stoppingConditions = new HashSet<StoppingCondition>();

	/**
	 * Add new fitness function (i.e., for new mutation)
	 *
	 * @param function
	 *            a {@link org.evosuite.ga.FitnessFunction} object.
	 */
	public void addFitnessFunction(FitnessFunction<T> function) {
		fitnessFunctions.add(function);
	}

	/**
	 * Add new fitness functions
	 *
	 * @param functions
	 */
	public void addFitnessFunctions(List<FitnessFunction<T>> functions) {
		for (FitnessFunction<T> function : functions)
			this.addFitnessFunction(function);
	}

	/**
	 * Get currently used fitness function
	 *
	 * @return a {@link org.evosuite.ga.FitnessFunction} object.
	 */
	public FitnessFunction<T> getFitnessFunction() {
		return fitnessFunctions.get(0);
	}

	/**
	 * Get all used fitness function
	 *
	 * @return a {@link org.evosuite.ga.FitnessFunction} object.
	 */
	public List<FitnessFunction<T>> getFitnessFunctions() {
		return fitnessFunctions;
	}

    /**
	 * Add a new search listener
	 *
	 * @param listener
	 *            a {@link org.evosuite.symbolic.DSE.algorithm.listener.SymbolicExecutionSearchListener}
	 *            object.
	 */
	public void addListener(SymbolicExecutionSearchListener listener) {
		listeners.add(listener);
	}

	/**
	 * Remove a search listener
	 *
	 * @param listener
	 *            a {@link org.evosuite.symbolic.DSE.algorithm.listener.SymbolicExecutionSearchListener}
	 *            object.
	 */
	public void removeListener(SymbolicExecutionSearchListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Notify all search listeners of search start
	 */
	protected void notifySearchStarted() {
		for (SymbolicExecutionSearchListener listener : listeners) {
			listener.searchStarted(this);
		}
	}

	/**
	 * Notify all search listeners of search end
	 */
	protected void notifySearchFinished() {
		for (SymbolicExecutionSearchListener listener : listeners) {
			listener.searchFinished(this);
		}
	}

	/**
	 * Notify all search listeners of iteration
	 */
	protected void notifyIteration() {
		for (SymbolicExecutionSearchListener listener : listeners) {
			listener.iteration(this);
		}
	}

	/**
	 * Notify all search listeners of fitness evaluation
	 *
	 * @param chromosome
	 *            a {@link org.evosuite.ga.Chromosome} object.
	 */
	protected void notifyEvaluation(Chromosome chromosome) {
		for (SymbolicExecutionSearchListener listener : listeners) {
			listener.fitnessEvaluation(chromosome);
		}
	}

	/**
	 * Determine whether any of the stopping conditions hold
	 *
	 * @return a boolean.
	 */
	public boolean isFinished() {
		for (StoppingCondition c : stoppingConditions) {
			if (c.isFinished())
				return true;
		}
		return false;
	}

	/**
	 * Returns the progress of the search.
	 *
	 * @return a value [0.0, 1.0]
	 */
	protected double progress() {
		long totalbudget = 0;
		long currentbudget = 0;

		for (StoppingCondition sc : this.stoppingConditions) {
			if (sc.getLimit() != 0) {
				totalbudget += sc.getLimit();
				currentbudget += sc.getCurrentValue();
			}
		}

		return (double) currentbudget / (double) totalbudget;
	}
}
