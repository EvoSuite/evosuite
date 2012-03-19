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
