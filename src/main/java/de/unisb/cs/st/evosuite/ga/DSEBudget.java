/**
 * 
 */
package de.unisb.cs.st.evosuite.ga;

import java.io.Serializable;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.Properties.DSEBudgetType;

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
