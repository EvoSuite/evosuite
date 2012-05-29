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

import de.unisb.cs.st.evosuite.ga.GeneticAlgorithm;

/**
 * Stop the search when the fitness has reached 0 (assuming minimization)
 * 
 * @author Gordon Fraser
 * 
 */
public class ZeroFitnessStoppingCondition extends StoppingConditionImpl {

	private static final long serialVersionUID = -6925872054053635256L;

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
	public void setLimit(long limit) {
		// Do nothing
	}

	@Override
	public long getLimit() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.StoppingCondition#getCurrentValue()
	 */
	@Override
	public long getCurrentValue() {
		return (long) (last_fitness + 0.5); // TODO: Why +0.5??
	}

	public void setFinished() {
		last_fitness = 0.0;
	}

	@Override
	public void forceCurrentValue(long value) {
		// TODO Auto-generated method stub
		// TODO ?
	}

}
