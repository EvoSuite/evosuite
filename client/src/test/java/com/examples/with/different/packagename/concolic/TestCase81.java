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

public class TestCase81 {

	/**
	 * @param args
	 */
	// double double0 = ConcolicMarker.mark(1.5, "double0");
	public static void test(double double0) {
		// box integer
		Double double_instance0 = box(double0);
		// unbox integer
		double double1 = unbox(double_instance0);
		double double2 = 1.5;
		checkEquals(double1, double2);
	}

	public static Double box(Double i) {
		return i;
	}

	public static double unbox(double i) {
		return i;
	}

}
