/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite contributors
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
/**
 * 
 */
package de.unisb.cs.st.evosuite.runtime;

/**
 * @author fraser
 * 
 */
public class System {

	private static boolean wasAccessed = false;

	/**
	 * This exception tells the test execution that it should stop at this point
	 */
	public static class SystemExitException extends RuntimeException {

		private static final long serialVersionUID = 1L;

	}

	/**
	 * Replacement function for System.exit
	 */
	public static void exit(int status) {
		wasAccessed = true;
		throw new SystemExitException();
	}

	/** Current time returns numbers increased by 1 */
	private static long currentTime = 0;

	/**
	 * Replacement function for System.currentTimeMillis
	 */
	public static long currentTimeMillis() {
		wasAccessed = true;
		return currentTime++;
	}

	/**
	 * Allow setting the time
	 * 
	 * @param time
	 */
	public static void setCurrentTimeMillis(long time) {
		currentTime = time;
	}

	/**
	 * Reset runtime to initial state
	 */
	public static void reset() {
		currentTime = 0;
		wasAccessed = false;
	}

	/**
	 * Getter to check whether this runtime replacement was accessed during test
	 * execution
	 * 
	 * @return
	 */
	public static boolean wasAccessed() {
		return wasAccessed;
	}
}
