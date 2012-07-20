package org.evosuite.symbolic;

import static org.evosuite.symbolic.Assertions.checkEquals;

import org.evosuite.symbolic.dsc.ConcolicMarker;

public class TestCase15 {

	private static final double DOUBLE_CONSTANT = 1.0;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		double double0 = ConcolicMarker.mark(DOUBLE_CONSTANT, "double0");
		double double1 = 2.0;
		double double2 = Math.atan2(double0, double1);
		double double3 = Math.atan2(DOUBLE_CONSTANT, double1);

		double double4 = Math.hypot(double0, double1);
		double double5 = Math.hypot(DOUBLE_CONSTANT, double1);

		double double6 = Math.IEEEremainder(double0, double1);
		double double7 = Math.IEEEremainder(DOUBLE_CONSTANT, double1);

		double double8 = Math.pow(double0, double1);
		double double9 = Math.pow(DOUBLE_CONSTANT, double1);

		checkEquals(double2, double3);
		checkEquals(double4, double5);
		checkEquals(double6, double7);
		checkEquals(double8, double9);
	}

}
