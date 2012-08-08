package org.evosuite.symbolic;

import org.evosuite.symbolic.dsc.ConcolicMarker;

public class TestCase56 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int int0 = ConcolicMarker.mark(1515, "int0");
		int int1 = 45451;
		if (int0 == int1) {
			return;
		}
		int int2 = ConcolicMarker.mark(1541, "int2");
		if (int2 == int0) {
			return;
		}

	}

}
