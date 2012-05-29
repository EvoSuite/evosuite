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
/**
 * 
 */
package de.unisb.cs.st.evosuite.runtime;

/**
 * @author Gordon Fraser
 * 
 */
public class Random {

	private static boolean wasAccessed = false;

	/**
	 * We have a unique number that is increased every time a new random number
	 * is accessed
	 */
	private static int currentNumber = 0;

	/**
	 * Replacement function for nextInt
	 * 
	 * @return
	 */
	public static int nextInt() {
		wasAccessed = true;
		return currentNumber++;
	}

	/**
	 * Replacement function for nextInt
	 * 
	 * @return
	 */
	public static int nextInt(int max) {
		wasAccessed = true;
		return currentNumber % max;
	}

	/**
	 * Replacement function for nextFloat
	 * 
	 * @return
	 */
	public static float nextFloat() {
		wasAccessed = true;
		return currentNumber++;
	}

	/**
	 * Replacement function for nextLong
	 * 
	 * @return
	 */
	public static long nextLong() {
		wasAccessed = true;
		return currentNumber++;
	}

	/**
	 * Set the next random number to a value
	 * 
	 * @param number
	 */
	public static void setNextRandom(int number) {
		currentNumber = number;
	}

	/**
	 * Reset runtime to initial state
	 */
	public static void reset() {
		currentNumber = 0;
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
