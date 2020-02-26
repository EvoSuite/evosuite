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
import org.evosuite.symbolic.DSE.algorithm.listener.SymbolicExecutionSearchListener;

import java.util.HashSet;
import java.util.Set;

/**
 * Abstract superclass of DSE algorithms
 *
 * @author Ignacio Lebrero
 */
public abstract class DSEBaseAlgorithm {

	/** Listeners */
	protected transient Set<SymbolicExecutionSearchListener> listeners = new HashSet();

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

//	/**
//	 * Determine whether any of the stopping conditions hold
//	 *
//	 * @return a boolean.
//	 */
//	public boolean isFinished() {
//		for (StoppingCondition c : stoppingConditions) {
//			// logger.error(c + " "+ c.getCurrentValue());
//			if (c.isFinished())
//				return true;
//		}
//		return false;
//	}
//
//	/**
//	 * Returns the progress of the search.
//	 *
//	 * @return a value [0.0, 1.0]
//	 */
//	protected double progress() {
//		long totalbudget = 0;
//		long currentbudget = 0;
//
//		for (StoppingCondition sc : this.stoppingConditions) {
//			if (sc.getLimit() != 0) {
//				totalbudget += sc.getLimit();
//				currentbudget += sc.getCurrentValue();
//			}
//		}
//
//		return (double) currentbudget / (double) totalbudget;
//	}
}
