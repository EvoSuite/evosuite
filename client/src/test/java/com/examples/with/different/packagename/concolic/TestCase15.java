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

public class TestCase15 {

	public static final double DOUBLE_CONSTANT = 1.0;

	/**
	 * @param args
	 */
	public static void test(double double0) {
		double double1 = 2.0;
		double double2 = Math.atan2(double0, double1);
		double double3 = Math.atan2(DOUBLE_CONSTANT, double1);

		double double4 = Math.hypot(double0, double1);
		double double5 = Math.hypot(DOUBLE_CONSTANT, double1);

		double double6 = Math.IEEEremainder(double0, double1);
		double double7 = Math.IEEEremainder(DOUBLE_CONSTANT, double1);

		double double8 = Math.pow(double0, double1);
		double double9 = Math.pow(DOUBLE_CONSTANT, double1);

		checkEquals(double2, double3);
		checkEquals(double4, double5);
		checkEquals(double6, double7);
		checkEquals(double8, double9);
	}

}
