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

public class TestCase17 {

	public static final float FLOAT_VALUE = -0.0099100191F;

	public static final double DOUBLE_VALUE = Math.PI;

	public static void test(float float0, double double0) {

		{
			// test getExponent(float,float)
			float float1 = FLOAT_VALUE;
			int int0 = Math.getExponent(float0);
			int int1 = Math.getExponent(float1);
			checkEquals(int0, int1);
		}
		{
			// test getExponent(double,double)
			double double1 = DOUBLE_VALUE;
			int int2 = Math.getExponent(double0);
			int int3 = Math.getExponent(double1);
			checkEquals(int2, int3);
		}
	}

}
