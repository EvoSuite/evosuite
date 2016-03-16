/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.runtime.thread;

public class KillSwitchHandler implements KillSwitch{

	private static final KillSwitchHandler singleton = new KillSwitchHandler();
	
	private volatile boolean kill;
	
	/**
	 * singleton constructor
	 */
	private KillSwitchHandler(){
		kill = false;
	}
	
	public static KillSwitchHandler getInstance(){
		return singleton;
	}

	@Override
	public void setKillSwitch(boolean kill) {
		this.kill = kill;
	}
	
	/**
	 * Throw an exception if kill switch is on
	 * @throws RuntimeException
	 */
	public void checkTimeout() throws RuntimeException{
		if(kill){
			throw new RuntimeException("Kill switch"); 
		}
	}
	
	/**
	 * Wrapper around {@link KillSwitchHandler#checkTimeout()} to simplify instrumentation
	 * @throws RuntimeException
	 */
	public static void killIfTimeout() throws RuntimeException {
		getInstance().checkTimeout();
	}
}
