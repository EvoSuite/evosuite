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

import de.unisb.cs.st.evosuite.ga.GeneticAlgorithm;

/**
 * Stop the search when the fitness has reached 0 (assuming minimization)
 * 
 * @author Gordon Fraser
 * 
 */
public class ZeroFitnessStoppingCondition extends StoppingCondition {

	/** Keep track of lowest fitness seen so far */
	private double last_fitness = Double.MAX_VALUE;

	/**
	 * Update information on currently lowest fitness
	 */
	@Override
	public void iteration(GeneticAlgorithm algorithm) {
		last_fitness = Math.min(last_fitness, algorithm.getBestIndividual().getFitness());
	}

	/**
	 * Returns true if best individual has fitness <= 0.0
	 */
	@Override
	public boolean isFinished() {
		return last_fitness <= 0.0;
	}

	/**
	 * Reset currently observed best fitness
	 */
	@Override
	public void reset() {
		last_fitness = Double.MAX_VALUE;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.StoppingCondition#setLimit(int)
	 */
	@Override
	public void setLimit(int limit) {
		// Do nothing
	}

	@Override
	public int getLimit() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.StoppingCondition#getCurrentValue()
	 */
	@Override
	public int getCurrentValue() {
		return (int) (last_fitness + 0.5);
	}

	public void setFinished() {
		last_fitness = 0.0;
	}

}
