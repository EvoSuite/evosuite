package org.evosuite.symbolic;

import org.evosuite.symbolic.dsc.ConcolicMarker;

public class TestCase11 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MathInt mathInt0 = new MathInt();
		int int0 = ConcolicMarker.mark(1,"var1");
		int int1 = ConcolicMarker.mark(4,"var2");
		int int2 = mathInt0.shiftLeft(int0, int1);
		int int3 = ConcolicMarker.mark(16,"var3");
		if (int2!=int3) {
			mathInt0.castToChar(int3);
		}
		int int4 = mathInt0.shiftRight(int0, int1);
		int int5 = ConcolicMarker.mark(0, "var4");
		if (int4!=int5) {
			mathInt0.castToChar(int3);
		}
		int int6 = mathInt0.unsignedShiftRight(int0, int1);
		if (int6!=int5) {
			mathInt0.castToChar(int3);
		}
	}

}
