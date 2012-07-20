package org.evosuite.symbolic;

import org.evosuite.symbolic.dsc.ConcolicMarker;

public class TestCase9 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MathInt mathInt0 = new MathInt();
		int int0 = ConcolicMarker.mark(0,"var1");
		int int1 = mathInt0.remainder(int0, int0);
	}

}
