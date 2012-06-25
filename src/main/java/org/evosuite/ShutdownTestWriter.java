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
package org.evosuite;

import org.evosuite.ga.stoppingconditions.StoppingConditionImpl;

import sun.misc.Signal;
import sun.misc.SignalHandler;

/**
 * @author Gordon Fraser
 * 
 */
@SuppressWarnings("restriction")
public class ShutdownTestWriter extends StoppingConditionImpl implements SignalHandler {

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
	public void setLimit(long limit) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.stoppingconditions.StoppingCondition#getLimit()
	 */
	@Override
	public long getLimit() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.stoppingconditions.StoppingCondition#getCurrentValue()
	 */
	@Override
	public long getCurrentValue() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void forceCurrentValue(long value) {
		// TODO Auto-generated method stub
		// TODO ?
	}

}
