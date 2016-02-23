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


public class TestCase5 {

	// float float0 = ConcolicMarker.mark(882.70544F,"var1");
	// float float1 = ConcolicMarker.mark(882.70544F,"var2");
	// float float2 = ConcolicMarker.mark(882.70544F, "var3");
	// float float3 = ConcolicMarker.mark(1.0F,"var4");
	// float float4 = ConcolicMarker.mark(63.534046F,"var5");
	public static void test(float float0, float float1, float float2,
			float float3, float float4) {
		MathFloat mathClass0 = new MathFloat();
		int int0 = mathClass0.castToInt(float0);
		mathClass0.unreach();
		float float5 = mathClass0.multiply(float4, float2);
		float float6 = mathClass0.divide(float3, float1);
		int int1;
		if (float5 > float6) {
			int1 = mathClass0.castToInt(float5);
		} else {
			int1 = mathClass0.castToInt(float6);
		}
	}
}
