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


public class TestCase6 {

	/**
	 * @param args
	 */
	// float float0 = ConcolicMarker.mark(1442.5817F,"var1");
	// float float1 = ConcolicMarker.mark(1.0F,"var2");
	public static void test(float float0, float float1) {
		MathFloat mathClass0 = new MathFloat();
		long long0 = mathClass0.castToLong(float0);
		short short0 = mathClass0.castToByte((float) long0);
		mathClass0.unreach();
		char char0 = mathClass0.castToChar(float1);
		float float2 = mathClass0.substract((float) char0, float1);
		if (float2 != 0) {
			mathClass0.castToInt(float2);
		}
	}

}
