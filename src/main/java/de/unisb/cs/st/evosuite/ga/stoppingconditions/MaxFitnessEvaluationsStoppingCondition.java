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
import de.unisb.cs.st.evosuite.ga.Chromosome;

/**
 * Stop search after a predefined maximum search depth
 * 
 * @author Gordon Fraser
 * 
 */
public class MaxFitnessEvaluationsStoppingCondition extends StoppingConditionImpl {
	
	private final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(MaxFitnessEvaluationsStoppingCondition.class);

	private static final long serialVersionUID = 208241490252275613L;

	/** Maximum number of evaluations */
	protected long max_evaluations = Properties.SEARCH_BUDGET;

	/** Maximum number of iterations */
	protected static long current_evaluation = 0;

	/**
	 * Stop when maximum number of fitness evaluations has been reached
	 */
	@Override
	public boolean isFinished() {
		logger.info("Current number of fitness_evaluations: " + current_evaluation);
		return current_evaluation >= max_evaluations;
	}

	/**
	 * Keep track of the number of fitness evaluations
	 */
	@Override
	public void fitnessEvaluation(Chromosome individual) {
		current_evaluation++;
	}

	/**
	 * Static getter method
	 */
	public static long getNumFitnessEvaluations() {
		return current_evaluation;
	}

	/**
	 * At the end, reset
	 */
	@Override
	public void reset() {
		current_evaluation = 0;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.StoppingCondition#setLimit(int)
	 */
	@Override
	public void setLimit(long limit) {
		max_evaluations = limit;
	}

	@Override
	public long getLimit() {
		return max_evaluations;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.StoppingCondition#getCurrentValue()
	 */
	@Override
	public long getCurrentValue() {
		return current_evaluation;
	}

	@Override
	public void forceCurrentValue(long value) {
		current_evaluation = value;
	}

}
