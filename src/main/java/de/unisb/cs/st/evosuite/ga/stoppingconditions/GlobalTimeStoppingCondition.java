/**
 * 
 */
package de.unisb.cs.st.evosuite.ga.stoppingconditions;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.ga.GeneticAlgorithm;

/**
 * @author Gordon Fraser
 * 
 */
public class GlobalTimeStoppingCondition extends StoppingConditionImpl {
	
	private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(GlobalTimeStoppingCondition.class);

	private static final long serialVersionUID = -4880914182984895075L;

	/** Assume the search has not started until start_time != 0 */
	protected static long start_time = 0L;
	
	protected static long pause_time = 0L;

	@Override
	public void searchStarted(GeneticAlgorithm algorithm) {
		if (start_time == 0)
			reset();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.StoppingCondition#getCurrentValue()
	 */
	@Override
	public long getCurrentValue() {
		long current_time = System.currentTimeMillis();
		return (int) ((current_time - start_time) / 1000);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.StoppingCondition#isFinished()
	 */
	@Override
	public boolean isFinished() {
		long current_time = System.currentTimeMillis();
		if (Properties.GLOBAL_TIMEOUT != 0 && start_time != 0
		        && (current_time - start_time) / 1000 > Properties.GLOBAL_TIMEOUT)
			logger.info("Timeout reached");

		return Properties.GLOBAL_TIMEOUT != 0 && start_time != 0
		        && (current_time - start_time) / 1000 > Properties.GLOBAL_TIMEOUT;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.StoppingCondition#reset()
	 */
	@Override
	public void reset() {
		if (start_time == 0)
			start_time = System.currentTimeMillis();
	}
	
	/**
	 * Fully resets the stopping condition. The start time is set to the current
	 * time and thus "no time has elapsed so far".
	 * If you want a conditional reset which only has an effect if the 
	 * start time has never been changed use <tt>reset()</tt>.
	 */
	public void fullReset() {
		start_time = System.currentTimeMillis();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.StoppingCondition#setLimit(int)
	 */
	@Override
	public void setLimit(long limit) {
		// TODO Auto-generated method stub

	}

	@Override
	public long getLimit() {
		// TODO Auto-generated method stub
		return Properties.GLOBAL_TIMEOUT;
	}

	public static void forceReset() {
		start_time = 0;
	}

	@Override
	public void forceCurrentValue(long value) {
		start_time = value;
	}
	
	/**
	 * Remember start pause time 
	 */
	public void pause() {
		pause_time = System.currentTimeMillis();
	}

	/**
	 * Change start time after MA
	 */
	public void resume() {
		start_time += System.currentTimeMillis() - pause_time;
	}

}
