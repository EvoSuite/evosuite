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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeDeltaStoppingCondition extends StoppingConditionImpl {

	private static final long serialVersionUID = -7029615280866928031L;

	/** Assume the search has not started until start_time != 0 */
	protected long startTime = 0L;

	/** Time at which the best fitness value was last improved */
	protected long lastImprovement = 0L;

	/** Time at which the fitness value was last checked */
	protected long lastGeneration = 0L;

	/** Best fitness value observed so far */
	protected double lastFitness = 0.0;
	
	private final static Logger logger = LoggerFactory.getLogger(TimeDeltaStoppingCondition.class);

	/** {@inheritDoc} */
	@Override
	public void searchStarted(GeneticAlgorithm<?> algorithm) {
		if(algorithm.getFitnessFunction().isMaximizationFunction()) {
			lastFitness = 0.0;
		} else {
			lastFitness = Double.MAX_VALUE;
		}
		startTime = System.currentTimeMillis();
		lastGeneration = 0L;
	}
	
	@Override
	public void iteration(GeneticAlgorithm<?> algorithm) {		
		double currentBestFitness = algorithm.getBestIndividual().getFitness();
		if(algorithm.getFitnessFunction().isMaximizationFunction()) {
			if(currentBestFitness > lastFitness) {
				lastFitness = currentBestFitness;
				lastImprovement = System.currentTimeMillis();
			}
		} else {
			if(currentBestFitness < lastFitness) {
				lastFitness = currentBestFitness;
				lastImprovement = System.currentTimeMillis();
			}			
		}
		lastGeneration = System.currentTimeMillis();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.StoppingCondition#getCurrentValue()
	 */
	/** {@inheritDoc} */
	@Override
	public long getCurrentValue() {
		long current_time = System.currentTimeMillis();
		return (int) ((current_time - startTime) / 1000);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.StoppingCondition#isFinished()
	 */
	/** {@inheritDoc} */
	@Override
	public boolean isFinished() {
		long current_time = System.currentTimeMillis();
		if (Properties.GLOBAL_TIMEOUT != 0 && startTime != 0
		        && (current_time - startTime) / 1000 > (Properties.GLOBAL_TIMEOUT)) {
			// Global timeout reached
			logger.info("Global timeout reached");
			return true;
		} else {
			// If we haven't even had one generation we might as well give up
			if(lastImprovement < startTime) {
				logger.info("Waiting for first generation.");
				return false;
			}
			
			// If we haven't managed to evolve one generation in the time since the last iteration, also continue
			if((current_time - lastGeneration) / 1000 > Properties.SEARCH_BUDGET) {
				logger.info("Waiting for at least a generation within the timeout.");
				return false;
			}
			
			// Check time since last improvement
			if((current_time - lastImprovement) / 1000 > Properties.SEARCH_BUDGET) {
				logger.info("No improvement timeout.");
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.StoppingCondition#reset()
	 */
	/** {@inheritDoc} */
	@Override
	public void reset() {
		if (startTime == 0)
			startTime = System.currentTimeMillis();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.StoppingCondition#setLimit(int)
	 */
	/** {@inheritDoc} */
	@Override
	public void setLimit(long limit) {
		// TODO Auto-generated method stub

	}

	/** {@inheritDoc} */
	@Override
	public long getLimit() {
		return Properties.GLOBAL_TIMEOUT;
	}

	@Override
	public void forceCurrentValue(long value) {
		// TODO Auto-generated method stub
		
	}

}
