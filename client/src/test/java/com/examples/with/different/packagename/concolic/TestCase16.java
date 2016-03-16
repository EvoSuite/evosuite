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


public class TestCase16 {

	public static void test(int int0, int int1, long long0, long long1,
			float float0, float float1, double double0, double double1) {
		{
			// test abs(int,int)
			int int2 = Math.abs(int0);
			checkEquals(int1, int2);
		}
		{
			// test abs(long,long)
			long long2 = Math.abs(long0);
			checkEquals(long1, long2);
		}
		{
			// test abs(float,float)
			float float2 = Math.abs(float0);
			checkEquals(float1, float2);
		}
		{
			// test abs(double,double)
			double double2 = Math.abs(double0);
			checkEquals(double1, double2);
		}
	}

}
