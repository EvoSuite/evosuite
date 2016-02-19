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
package com.examples.with.different.packagename.concolic;

import static com.examples.with.different.packagename.concolic.Assertions.checkEquals;

public class TestCase42 {

	private int[] intArray;

	/**
	 * @param args
	 */
	public static void test() {

		TestCase42 instance = new TestCase42();

		int[] myArray = new int[10];
		myArray[9] = Integer.MAX_VALUE;

		instance.intArray = myArray;
		instance.intArray[0] = Integer.MAX_VALUE;
		instance.intArray[1] = Integer.MIN_VALUE;
		instance.intArray[2] = (int) (Math.PI * 1000000);

		myArray[3] = (int) (Math.E * 1000000);

		int[] otherArray = new int[5];
		otherArray[3] = Integer.MAX_VALUE / 2;

		checkEquals(myArray[0], instance.intArray[0]);

	}

}
