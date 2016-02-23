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


public class TestCase11 {

	/**
	 * @param args
	 */
	public static void test(int int0, int int1, int int3, int int5) {
		MathInt mathInt0 = new MathInt();
		int int2 = mathInt0.shiftLeft(int0, int1);
		if (int2 != int3) {
			mathInt0.castToChar(int3);
		}
		int int4 = mathInt0.shiftRight(int0, int1);
		if (int4 != int5) {
			mathInt0.castToChar(int3);
		}
		int int6 = mathInt0.unsignedShiftRight(int0, int1);
		if (int6 != int5) {
			mathInt0.castToChar(int3);
		}
	}

}
