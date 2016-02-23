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
package com.examples.with.different.packagename.stable;

import java.security.SecureRandom;

public class SecureRandomUser {

	private final SecureRandom secureRandom;

	public SecureRandomUser() {
		secureRandom = new java.security.SecureRandom();
	}

	public String toString() {
		return secureRandom.toString();
	}

	public int nextInt() {
		return secureRandom.nextInt();
	}

	public long nextLong() {
		return secureRandom.nextLong();
	}

	public int nextInt(int n) {
		return secureRandom.nextInt(n);
	}

	public boolean nextBoolean() {
		return secureRandom.nextBoolean();
	}

	public double nextDouble() {
		return secureRandom.nextDouble();
	}

	public void nextBytes(byte[] bytes) {
		secureRandom.nextBytes(bytes);
	}

	public float nextFloat() {
		return secureRandom.nextFloat();
	}

	public double nextGaussian() {
		return secureRandom.nextGaussian();
	}

}
