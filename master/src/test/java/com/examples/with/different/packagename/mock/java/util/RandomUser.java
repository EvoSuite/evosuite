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
package com.examples.with.different.packagename.mock.java.util;

import java.util.Random;

public class RandomUser {

	private final byte[] bytes0;
	private final double double0;
	private final boolean boolean0;
	public RandomUser() {
		Random random = new Random();
		double0 = random.nextGaussian();
		boolean0 = random.nextBoolean();
		bytes0 = new byte[5];
		random.nextBytes(new byte[5]);
	}

	public byte[] getBytes0() {
		return bytes0;
	}

	public double getDouble0() {
		return double0;
	}

	public boolean getBoolean0() {
		return boolean0;
	}

}
