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
package com.examples.with.different.packagename.testcarver;

import org.junit.Test;

public class PrimitivesTest {

	@Test
	public void test() {
		ObjectWrapper wrapper = new ObjectWrapper();

		int zero = 0;
		Integer one = 1;
		char two = '2';
		float three = 3f;
		double four = 4d;
		long five = 5l;
		byte six = 6;
		String seven = "7";

		String s = "" + zero + one + two + three + four + five + six + seven;

		wrapper.set(s);
		wrapper.set(zero);
		wrapper.set(one);
		wrapper.set(two);
		wrapper.set(three);
		wrapper.set(four);
		wrapper.set(five);
		wrapper.set(six);
		wrapper.set(seven);
	}
}
