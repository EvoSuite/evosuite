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

/**
 * <p>MaxTestsStoppingCondition class.</p>
 *
 * @author Gordon Fraser
 */
public class MaxTestsStoppingCondition extends StoppingConditionImpl {

	private static final long serialVersionUID = -3375236459377313641L;

	/** Current number of tests */
	protected static long numTests = 0;

	/** Maximum number of evaluations */
	protected long maxTests = Properties.SEARCH_BUDGET;

	/**
	 * <p>getNumExecutedTests</p>
	 *
	 * @return a long.
	 */
	public static long getNumExecutedTests() {
		return numTests;
	}

	/**
	 * <p>testExecuted</p>
	 */
	public static void testExecuted() {
		numTests++;
	}

	/** {@inheritDoc} */
	@Override
	public void reset() {
		numTests = 0;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isFinished() {
		return numTests >= maxTests;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.StoppingCondition#getCurrentValue()
	 */
	/** {@inheritDoc} */
	@Override
	public long getCurrentValue() {
		return numTests;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.StoppingCondition#setLimit(int)
	 */
	/** {@inheritDoc} */
	@Override
	public void setLimit(long limit) {
		maxTests = limit;
	}

	/** {@inheritDoc} */
	@Override
	public long getLimit() {
		return maxTests;
	}

	/** {@inheritDoc} */
	@Override
	public void forceCurrentValue(long value) {
		// TODO Auto-generated method stub
		numTests = value;
	}

}
