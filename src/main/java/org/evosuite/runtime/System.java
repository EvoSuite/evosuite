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
package org.evosuite.runtime;

import java.util.HashMap;
import java.util.Map;



/**
 * <p>
 * System class.
 * </p>
 * 
 * @author fraser
 */
public class System {

	private static boolean wasAccessed = false;

	/**
	 * <p >
	 * This exception tells the test execution that it should stop at this point
	 * </p>
	 * 
	 * <p>
	 * Note that it extends {@code Error}, as we need something that is
	 * unchecked
	 * </p>
	 */
	public static class SystemExitException extends Error {

		private static final long serialVersionUID = 1L;

	}

	/**
	 * Replacement function for System.exit
	 * 
	 * @param status
	 *            a int.
	 */
	public static void exit(int status) {
		wasAccessed = true;

		/*
		 * TODO: Here we could handle the calls to the JVM shutdown hooks, if any is present
		 */

		throw new SystemExitException();
	}

	/** Current time returns numbers increased by 1 */
	// Initialised to 2014-02-14, 20:21
	private static long currentTime = 1392409281320L;

	/**
	 * Replacement function for System.currentTimeMillis
	 * 
	 * @return a long.
	 */
	public static long currentTimeMillis() {
		wasAccessed = true;
		return currentTime; //++;
	}
	
	private static Map<Integer, Integer> hashKeys = new HashMap<Integer, Integer>();
	
	public static void registerObjectForIdentityHashCode(Object o) {
		identityHashCode(o);
	}
	
	public static int identityHashCode(Object o) {
		if(o == null)
			return 0;
		
		Integer realId = java.lang.System.identityHashCode(o);
		if(!hashKeys.containsKey(realId))
			hashKeys.put(realId, hashKeys.size() + 1);
		
		return hashKeys.get(realId);
	}

	/**
	 * Replacement function for System.currentTimeMillis
	 * 
	 * @return a long.
	 */
	public static long nanoTime() {
		wasAccessed = true;
		return currentTime * 1000; //++;
	}

	/**
	 * Allow setting the time
	 * 
	 * @param time
	 *            a long.
	 */
	public static void setCurrentTimeMillis(long time) {
		currentTime = time;
	}

	/**
	 * Reset runtime to initial state
	 */
	public static void reset() {
		currentTime = 1392409281320L; // 2014-02-14, 20:21
		wasAccessed = false;
		hashKeys.clear();
	}

	/**
	 * Getter to check whether this runtime replacement was accessed during test
	 * execution
	 * 
	 * @return a boolean.
	 */
	public static boolean wasAccessed() {
		return wasAccessed;
	}
}
