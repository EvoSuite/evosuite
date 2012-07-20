package org.evosuite.symbolic;

import org.evosuite.symbolic.dsc.ConcolicMarker;

public class TestCase13 {

	private static final double DOUBLE_VALUE = Math.E;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		double double0 = ConcolicMarker.mark(DOUBLE_VALUE, "double0");
		double double1 = DOUBLE_VALUE;
		double double2 = Math.cos(double0);
		double double3 = Math.cos(double1);
		
		if (double2!=double3) {
			throw new RuntimeException();
		}
		
	}
}
