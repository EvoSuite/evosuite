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
import static com.examples.with.different.packagename.concolic.Assertions.checkObjectEquals;

public class TestCase45 {

	/**
	 * @param args
	 */
	public static void test() {
		boolean[] boolArray = new boolean[12];
		boolean defaultValue = boolArray[0];
		checkEquals(false, defaultValue);

		TestCase45[] objectArray = new TestCase45[12];
		TestCase45 objectDefaultValue = objectArray[0];
		checkObjectEquals(null, objectDefaultValue);

		long[] longArray = new long[12];
		long longDefaultValue = longArray[0];
		checkEquals(0, longDefaultValue);
	}

}
