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

public class TestCase38 {

	/**
	 * @param args
	 */
	public static void test(int int0, boolean boolean0, short short0,
			byte byte0, char char0, long long0, float float0, double double0) {

		boolean[] booleanArray0 = new boolean[int0];
		short[] shortArray0 = new short[int0];
		byte[] byteArray0 = new byte[int0];
		char[] charArray0 = new char[int0];
		long[] longArray0 = new long[int0];
		float[] floatArray0 = new float[int0];
		double[] doubleArray0 = new double[int0];

		booleanArray0[0] = boolean0;
		shortArray0[0] = short0;
		byteArray0[0] = byte0;
		charArray0[0] = char0;
		longArray0[0] = long0;
		floatArray0[0] = float0;
		doubleArray0[0] = double0;

		checkEquals(booleanArray0[0], Boolean.TRUE);
		checkEquals(shortArray0[0], Short.MAX_VALUE);
		checkEquals(byteArray0[0], Byte.MAX_VALUE);
		checkEquals(charArray0[0], Character.MAX_VALUE);
		checkEquals(longArray0[0], Long.MAX_VALUE);
		checkEquals(floatArray0[0], Float.MAX_VALUE);
		checkEquals(doubleArray0[0], Double.MAX_VALUE);

	}
}
