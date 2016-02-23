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
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stop search when a maximum (average) length has been reached. Used for
 * experiments on length bloat.
 * 
 * @author Gordon Fraser
 */
public class MaxLengthStoppingCondition extends StoppingConditionImpl {

	private static final Logger logger = LoggerFactory.getLogger(MaxLengthStoppingCondition.class);

	private static final long serialVersionUID = 8537667219135128366L;

	private double averageLength = 0.0;
	private int maxLength = Properties.MAX_LENGTH;

	/* (non-Javadoc)
	 * @see org.ga.StoppingCondition#isFinished()
	 */
	/** {@inheritDoc} */
	@Override
	public boolean isFinished() {
		if (averageLength >= maxLength)
			logger.info("Maximum average length reached, stopping");
		return averageLength >= maxLength;
	}

	/* (non-Javadoc)
	 * @see org.ga.StoppingCondition#reset()
	 */
	/** {@inheritDoc} */
	@Override
	public void reset() {
		averageLength = 0.0;
		maxLength = Properties.MAX_LENGTH;
	}

	/** {@inheritDoc} */
	@Override
	public void iteration(GeneticAlgorithm<?> algorithm) {
		double avg = 0.0;
		for (Chromosome c : algorithm.getPopulation()) {
			avg += c.size();
		}
		averageLength = avg / algorithm.getPopulation().size();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.StoppingCondition#getCurrentValue()
	 */
	/** {@inheritDoc} */
	@Override
	public long getCurrentValue() {
		return (long) averageLength;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.StoppingCondition#setLimit(int)
	 */
	/** {@inheritDoc} */
	@Override
	public void setLimit(long limit) {
		maxLength = (int) limit;
	}

	/** {@inheritDoc} */
	@Override
	public long getLimit() {
		return (long) (maxLength + 0.5);
	}

	/** {@inheritDoc} */
	@Override
	public void forceCurrentValue(long value) {
		// TODO Auto-generated method stub
		// TODO ?
	}
}
