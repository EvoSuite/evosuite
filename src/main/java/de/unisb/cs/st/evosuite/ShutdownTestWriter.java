/**
 * 
 */
package de.unisb.cs.st.evosuite;

import sun.misc.Signal;
import sun.misc.SignalHandler;
import de.unisb.cs.st.evosuite.ga.stoppingconditions.StoppingCondition;

/**
 * @author Gordon Fraser
 * 
 */
@SuppressWarnings("restriction")
public class ShutdownTestWriter extends StoppingCondition implements SignalHandler {

	private static final long serialVersionUID = -5703624299360241009L;

	private static boolean interrupted = false;

	/* (non-Javadoc)
	 * @see sun.misc.SignalHandler#handle(sun.misc.Signal)
	 */
	@Override
	public void handle(Signal arg0) {
		System.out.println("\n* User requested search stop");

		// If this is the second Ctrl+C the user _really_ wants to stop...
		if (interrupted)
			System.exit(0);
		interrupted = true;
	}

	public static boolean isInterrupted() {
		return interrupted;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.stoppingconditions.StoppingCondition#isFinished()
	 */
	@Override
	public boolean isFinished() {
		return interrupted;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.stoppingconditions.StoppingCondition#reset()
	 */
	@Override
	public void reset() {
		// interrupted = false;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.stoppingconditions.StoppingCondition#setLimit(int)
	 */
	@Override
	public void setLimit(int limit) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.stoppingconditions.StoppingCondition#getLimit()
	 */
	@Override
	public int getLimit() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.stoppingconditions.StoppingCondition#getCurrentValue()
	 */
	@Override
	public int getCurrentValue() {
		// TODO Auto-generated method stub
		return 0;
	}

}
