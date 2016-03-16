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
package org.evosuite.ga.stoppingconditions;

import org.evosuite.Properties;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;

/**
 * Stop search after a predefined number of iterations
 * 
 * @author Gordon Fraser
 */
public class MaxGenerationStoppingCondition extends StoppingConditionImpl {

	private static final long serialVersionUID = 251196904115160351L;

	/** Maximum number of iterations */
	protected long maxIterations = Properties.SEARCH_BUDGET;

	/** Maximum number of iterations */
	protected long currentIteration = 0;

	/**
	 * <p>
	 * setMaxIterations
	 * </p>
	 * 
	 * @param max
	 *            a int.
	 */
	public void setMaxIterations(int max) {
		maxIterations = max;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Increase iteration counter
	 */
	@Override
	public void iteration(GeneticAlgorithm<?> algorithm) {
		currentIteration++;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Stop search after a number of iterations
	 */
	@Override
	public boolean isFinished() {
		return currentIteration >= maxIterations;
	}

	/** {@inheritDoc} */
	@Override
	public void searchFinished(GeneticAlgorithm<?> algorithm) {
		currentIteration = 0;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Reset counter
	 */
	@Override
	public void reset() {
		currentIteration = 0;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.StoppingCondition#setLimit(int)
	 */
	/** {@inheritDoc} */
	@Override
	public void setLimit(long limit) {
		maxIterations = limit;
	}

	/** {@inheritDoc} */
	@Override
	public long getLimit() {
		return maxIterations;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.StoppingCondition#getCurrentValue()
	 */
	/** {@inheritDoc} */
	@Override
	public long getCurrentValue() {
		return currentIteration;
	}

	/** {@inheritDoc} */
	@Override
	public void forceCurrentValue(long value) {
		currentIteration = value;
	}
}
