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

import org.evosuite.ga.metaheuristics.GeneticAlgorithm;

/**
 * Stop the search when the fitness has reached 0 (assuming minimization)
 *
 * @author Gordon Fraser
 */
public class ZeroFitnessStoppingCondition extends StoppingConditionImpl {

	private static final long serialVersionUID = -6925872054053635256L;

	/** Keep track of lowest fitness seen so far */
	private double lastFitness = Double.MAX_VALUE;

	/**
	 * {@inheritDoc}
	 *
	 * Update information on currently lowest fitness
	 */
	@Override
	public void iteration(GeneticAlgorithm<?> algorithm) {
		lastFitness = Math.min(lastFitness, algorithm.getBestIndividual().getFitness());
	}

	/**
	 * {@inheritDoc}
	 *
	 * Returns true if best individual has fitness <= 0.0
	 */
	@Override
	public boolean isFinished() {
		return lastFitness <= 0.0;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Reset currently observed best fitness
	 */
	@Override
	public void reset() {
		lastFitness = Double.MAX_VALUE;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.StoppingCondition#setLimit(int)
	 */
	/** {@inheritDoc} */
	@Override
	public void setLimit(long limit) {
		// Do nothing
	}

	/** {@inheritDoc} */
	@Override
	public long getLimit() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.StoppingCondition#getCurrentValue()
	 */
	/** {@inheritDoc} */
	@Override
	public long getCurrentValue() {
		return (long) (lastFitness + 0.5); // TODO: Why +0.5??
	}

	/**
	 * <p>setFinished</p>
	 */
	public void setFinished() {
		lastFitness = 0.0;
	}

	/** {@inheritDoc} */
	@Override
	public void forceCurrentValue(long value) {
		// TODO Auto-generated method stub
		// TODO ?
	}

}
