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

package de.unisb.cs.st.evosuite.ga.stoppingconditions;

import de.unisb.cs.st.evosuite.Properties;

/**
 * @author Gordon Fraser
 * 
 */
public class MaxTestsStoppingCondition extends StoppingConditionImpl {

	private static final long serialVersionUID = -3375236459377313641L;

	/** Current number of tests */
	protected static long num_tests = 0;

	/** Maximum number of evaluations */
	protected long max_tests = Properties.GENERATIONS;

	public static long getNumExecutedTests() {
		return num_tests;
	}

	public static void testExecuted() {
		num_tests++;
	}

	@Override
	public void reset() {
		num_tests = 0;
	}

	@Override
	public boolean isFinished() {
		return num_tests >= max_tests;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.StoppingCondition#getCurrentValue()
	 */
	@Override
	public long getCurrentValue() {
		return num_tests;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.StoppingCondition#setLimit(int)
	 */
	@Override
	public void setLimit(long limit) {
		max_tests = limit;
	}

	@Override
	public long getLimit() {
		return max_tests;
	}

	@Override
	public void forceCurrentValue(long value) {
		// TODO Auto-generated method stub
		num_tests = value;
	}

}
