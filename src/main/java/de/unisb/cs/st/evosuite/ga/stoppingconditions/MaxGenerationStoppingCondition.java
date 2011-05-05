/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of the GA library.
 * 
 * GA is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * GA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with
 * GA. If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.ga.stoppingconditions;

import java.util.List;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.ga.Chromosome;

/**
 * Stop search after a predefined number of iterations
 * 
 * @author Gordon Fraser
 * 
 */
public class MaxGenerationStoppingCondition extends StoppingCondition {

	/** Maximum number of iterations */
	protected int max_iterations = Properties.GENERATIONS;

	/** Maximum number of iterations */
	protected int current_iteration = 0;

	public void setMaxIterations(int max) {
		max_iterations = max;
	}

	/**
	 * Increase iteration counter
	 */
	@Override
	public void iteration(List<Chromosome> population) {
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
	public void searchFinished(List<Chromosome> population) {
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
	 * @see de.unisb.cs.st.ga.SearchListener#fitnessEvaluation(java.util.List)
	 */
	@Override
	public void fitnessEvaluation(Chromosome individual) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.StoppingCondition#setLimit(int)
	 */
	@Override
	public void setLimit(int limit) {
		max_iterations = limit;
	}

	@Override
	public int getLimit() {
		return max_iterations;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.StoppingCondition#getCurrentValue()
	 */
	@Override
	public int getCurrentValue() {
		return current_iteration;
	}

}
