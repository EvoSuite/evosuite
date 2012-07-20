package org.evosuite.symbolic;

import org.evosuite.symbolic.dsc.ConcolicMarker;

public class TestCase8 {

	public static void main(String[] args) {
		MathInt mathInt0 = new MathInt();
		mathInt0.unreach();
		MathInt mathInt1 = new MathInt();
		int int0 = ConcolicMarker.mark(-1,"var1");
		int int1 = mathInt1.sum(int0, int0);
		int int2 = mathInt1.divide(int1, int1);
		int int3 = mathInt1.substract(int1, int1);
		mathInt0.unreach();
	}
}
