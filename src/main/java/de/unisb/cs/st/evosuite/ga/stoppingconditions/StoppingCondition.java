package de.unisb.cs.st.evosuite.ga.stoppingconditions;

import de.unisb.cs.st.evosuite.ga.SearchListener;

public interface StoppingCondition extends SearchListener {

	/**
	 * How much of the budget have we used up
	 * 
	 * @return
	 */
	int getCurrentValue();

	/**
	 * Get upper limit of resources
	 * 
	 * Mainly used for toString()
	 * 
	 * @return limit
	 */
	int getLimit();

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
	void setLimit(int limit);
}
