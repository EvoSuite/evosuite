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
package de.unisb.cs.st.evosuite.ga.stoppingconditions;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.ga.GeneticAlgorithm;

/**
 * Stop search after a predefined number of iterations
 * 
 * @author Gordon Fraser
 * 
 */
public class MaxGenerationStoppingCondition extends StoppingConditionImpl {
	
	private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MaxGenerationStoppingCondition.class);

	private static final long serialVersionUID = 251196904115160351L;

	/** Maximum number of iterations */
	protected long max_iterations = Properties.SEARCH_BUDGET;

	/** Maximum number of iterations */
	protected long current_iteration = 0;

	public void setMaxIterations(int max) {
		max_iterations = max;
	}

	/**
	 * Increase iteration counter
	 */
	@Override
	public void iteration(GeneticAlgorithm algorithm) {
		current_iteration++;
	}

	/**
	 * Stop search after a number of iterations
	 */
	@Override
	public boolean isFinished() {
		logger.debug("Is finished? Current generation: " + current_iteration
		        + " Max iteration: " + max_iterations);
		return current_iteration >= max_iterations;
	}

	@Override
	public void searchFinished(GeneticAlgorithm algorithm) {
		current_iteration = 0;
	}

	/**
	 * Reset counter
	 */
	@Override
	public void reset() {
		current_iteration = 0;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.StoppingCondition#setLimit(int)
	 */
	@Override
	public void setLimit(long limit) {
		max_iterations = limit;
	}

	@Override
	public long getLimit() {
		return max_iterations;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.StoppingCondition#getCurrentValue()
	 */
	@Override
	public long getCurrentValue() {
		return current_iteration;
	}

	@Override
	public void forceCurrentValue(long value) {
		current_iteration = value;
	}
}
