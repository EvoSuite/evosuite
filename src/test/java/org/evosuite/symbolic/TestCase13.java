package org.evosuite.symbolic;

public class TestCase13 {

	static final double DOUBLE_VALUE = Math.E;

	/**
	 * @param args
	 */
	public static void test(double double0) {
		double double1 = DOUBLE_VALUE;
		double double2 = Math.cos(double0);
		double double3 = Math.cos(double1);

		if (double2 != double3) {
			throw new RuntimeException();
		}

	}
}
