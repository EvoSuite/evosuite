package de.unisb.cs.st.evosuite.ga.stoppingconditions;

import de.unisb.cs.st.evosuite.ga.SearchListener;

public interface StoppingCondition extends SearchListener {

	/**
	 * Force a specific amount of used up budget. Handle with care!
	 * 
	 * @param value
	 *            The new amount of used up budget for this StoppingCondition
	 */
	public abstract void forceCurrentValue(long value);

	/**
	 * How much of the budget have we used up
	 * 
	 * @return
	 */
	public abstract long getCurrentValue();

	/**
	 * Get upper limit of resources
	 * 
	 * Mainly used for toString()
	 * 
	 * @return limit
	 */
	public abstract long getLimit();

	boolean isFinished();

	/**
	 * Reset everything
	 */
	void reset();

	/**
	 * Set new upper limit of resources
	 * 
	 * @param limit
	 */
	void setLimit(long limit);
}
