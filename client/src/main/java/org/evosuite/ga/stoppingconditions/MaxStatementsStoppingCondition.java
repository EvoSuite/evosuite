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
/**
 * 
 */
/*
 * Copyright (C) 2010 Saarland University
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
 * You should have received a copy of the GNU Lesser Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */

package org.evosuite.ga.stoppingconditions;

import org.evosuite.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * MaxStatementsStoppingCondition class.
 * </p>
 * 
 * @author Gordon Fraser
 */
public class MaxStatementsStoppingCondition extends StoppingConditionImpl {

	private static final long serialVersionUID = 8521297417505862683L;

	@SuppressWarnings({ "unused" })
	private static final Logger logger = LoggerFactory.getLogger(MaxStatementsStoppingCondition.class);

	/** Maximum number of iterations */
	protected static long currentStatement = 0;

	/**
	 * Add a given number of executed statements
	 * 
	 * @param num
	 *            a int.
	 */
	public static void statementsExecuted(int num) {
		currentStatement += num;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Finished, if the maximum number of statements has been reached
	 */
	@Override
	public boolean isFinished() {
		// logger.info("Current number of statements executed: " + current_statement + "/"
		//        + max_statements);
		return currentStatement >= Properties.SEARCH_BUDGET;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Reset counter
	 */
	@Override
	public void reset() {
		currentStatement = 0;
	}

	/**
	 * <p>
	 * getNumExecutedStatements
	 * </p>
	 * 
	 * @return a long.
	 */
	public static long getNumExecutedStatements() {
		return currentStatement;
	}

	/**
	 * <p>
	 * getNumExecutedStatements
	 * </p>
	 * 
	 * @return a long.
	 */
	public static void setNumExecutedStatements(long value) {
		currentStatement = value;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.StoppingCondition#getCurrentValue()
	 */
	/** {@inheritDoc} */
	@Override
	public long getCurrentValue() {
		return currentStatement;
	}

	/** {@inheritDoc} */
	@Override
	public long getLimit() {
		return Properties.SEARCH_BUDGET;
	}

	/** {@inheritDoc} */
	@Override
	public void forceCurrentValue(long value) {
		currentStatement = value;
	}

	@Override
	public void setLimit(long limit) {
		// No-op?
		// The limit should be set by setting Properties.SEARCH_BUDGET
	}

}
