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


public class TestInput1 {

	/**
	 * 
	 * @param int0
	 *            ==-15
	 * @param long0
	 *            ==Long.MAX_VALUE
	 * @param string0
	 *            .equals("Togliere sta roba")
	 */
	public static void test(int int0, long long0, String string0) {
		int int1 = Math.abs(int0);
		int int2 = (int) Math.min(int1, long0);
		int int3 = string0.length();

		int int4 = (int) Math.min(Math.abs(-15), Long.MAX_VALUE);
		Assertions.checkEquals(int4, int2);

		int int5 = new String("Togliere sta roba").length();
		Assertions.checkEquals(int5, int3);

	}
}
