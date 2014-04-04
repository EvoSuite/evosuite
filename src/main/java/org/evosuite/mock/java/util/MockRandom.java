package org.evosuite.mock.java.util;

import java.util.Random;

public class MockRandom extends Random {

	private static final long serialVersionUID = 7095505244285248683L;

	public MockRandom() {
		super(0);
	}
	
	public MockRandom(long seed) {
		super(seed);
	}	

	/**
	 * Replacement function for nextInt
	 * 
	 * @return a int.
	 */
	public int nextInt() {
		return org.evosuite.runtime.Random.nextInt();
	}

	/**
	 * Replacement function for nextInt
	 * 
	 * @param max
	 *            a int.
	 * @return a int.
	 */
	public int nextInt(int max) {
		return org.evosuite.runtime.Random.nextInt(max);
	}

	/**
	 * Replacement function for nextFloat
	 * 
	 * @return a float.
	 */
	public float nextFloat() {
		return org.evosuite.runtime.Random.nextFloat();
	}
	

	/**
	 * Replacement function for nextBytes
	 * @param bytes
	 */
	 public void nextBytes(byte[] bytes) {
		org.evosuite.runtime.Random.nextBytes(bytes);
	 }

	 
	/**
	 * Replacement function for nextDouble
	 * 
	 * @return a float.
	 */
	public double nextDouble() {
		return org.evosuite.runtime.Random.nextDouble();
	}

	/**
	 * Replacement function for nextGaussian
	 * 
	 * @return a double.
	 */
	public double nextGaussian() {
		return org.evosuite.runtime.Random.nextGaussian();
	}
	
	/**
	 * Replacement function for nextBoolean
	 * 
	 * @return a boolean.
	 */
	public boolean nextBoolean() {
		return org.evosuite.runtime.Random.nextBoolean();
	}

	
	/**
	 * Replacement function for nextLong
	 * 
	 * @return a long.
	 */
	public long nextLong() {
		return org.evosuite.runtime.Random.nextLong();
	}
}
