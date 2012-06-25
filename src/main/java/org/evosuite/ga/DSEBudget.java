/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package org.evosuite.ga;

import java.io.Serializable;

import org.evosuite.Properties;
import org.evosuite.Properties.DSEBudgetType;


/**
 * @author fraser
 * 
 */
public class DSEBudget implements Serializable {

	private static final long serialVersionUID = -2513164673911510952L;

	private static int attempts = 0;

	protected static long startTime = 0L;

	protected static long endTime = 0L;

	protected static long lastIndividualFinished = 0L;

	public static void DSEStarted() {
		startTime = System.currentTimeMillis();
		endTime = startTime + Properties.DSE_BUDGET;
		attempts = 0;
		lastIndividualFinished = startTime;
	}

	public static boolean isFinished() {
		if (Properties.DSE_BUDGET_TYPE == DSEBudgetType.INDIVIDUALS)
			return attempts >= Properties.DSE_BUDGET;
		else if (Properties.DSE_BUDGET_TYPE == DSEBudgetType.TIME)
			return System.currentTimeMillis() > endTime;
		else
			throw new RuntimeException("Unknown budget type: "
			        + Properties.DSE_BUDGET_TYPE);
	}

	public static boolean isHalfRemaining() {
		if (Properties.DSE_BUDGET_TYPE != DSEBudgetType.TIME)
			return false;

		long remaining = endTime - lastIndividualFinished;
		long now = System.currentTimeMillis();
		if (now > lastIndividualFinished + remaining / 2)
			return true;

		return false;
	}

	public static void evaluation() {
		attempts++;
		lastIndividualFinished = System.currentTimeMillis();
	}
}
