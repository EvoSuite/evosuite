/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.runtime.mock.java.util;

import java.util.Random;

import org.evosuite.runtime.mock.OverrideMock;

public class MockRandom extends Random  implements OverrideMock{

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
