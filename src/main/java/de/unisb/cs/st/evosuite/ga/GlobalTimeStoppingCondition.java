/**
 * 
 */
package de.unisb.cs.st.evosuite.ga;

import de.unisb.cs.st.evosuite.Properties;

/**
 * @author Gordon Fraser
 *
 */
public class GlobalTimeStoppingCondition extends StoppingCondition {

	/** Maximum number of seconds. 0 = infinite time */
	protected static int max_seconds = Properties.getPropertyOrDefault("global_timeout", 600);

	/** Assume the search has not started until start_time != 0 */
	protected static long start_time = 0L;
	
	public void searchStarted(FitnessFunction objective) {
		if(start_time == 0)
			start_time = System.currentTimeMillis();
	}
	
	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.StoppingCondition#getCurrentValue()
	 */
	@Override
	public int getCurrentValue() {
		long current_time = System.currentTimeMillis();
		return (int)((current_time - start_time) / 1000);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.StoppingCondition#isFinished()
	 */
	@Override
	public boolean isFinished() {
		long current_time = System.currentTimeMillis();
		if(max_seconds != 0 && start_time != 0 && (current_time - start_time) / 1000 > max_seconds)
			logger.info("Timeout reached");
		/*
		else
			logger.info("Timeout not reached: "+getCurrentValue());
			*/
		return max_seconds != 0 && start_time != 0 && (current_time - start_time) / 1000 > max_seconds;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.StoppingCondition#reset()
	 */
	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.StoppingCondition#setLimit(int)
	 */
	@Override
	public void setLimit(int limit) {
		// TODO Auto-generated method stub

	}

}
