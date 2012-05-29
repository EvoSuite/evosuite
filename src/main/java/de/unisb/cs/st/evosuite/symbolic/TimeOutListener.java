/**
 * Copyright (C) 2012 Gordon Fraser, Andrea Arcuri
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
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package de.unisb.cs.st.evosuite.symbolic;

import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.jvm.JVM;

/**
 * 
 * @author Jan Malburg
 * 
 */
public class TimeOutListener extends ListenerAdapter {
	private final long end;
	private boolean timeOut = false;

	public TimeOutListener(long timeTillEnd) {
		this.end = System.currentTimeMillis() + timeTillEnd;
	}

	@Override
	public void instructionExecuted(JVM vm) {
		if (end < System.currentTimeMillis()) {
			vm.breakTransition();
			timeOut = true;
		}

	}

	public boolean isTimeOut() {
		return timeOut;
	}

}
