package com.examples.with.different.packagename.concolic;

import org.evosuite.symbolic.MathInt;

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
