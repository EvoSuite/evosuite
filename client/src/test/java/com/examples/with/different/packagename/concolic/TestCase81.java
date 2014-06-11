package com.examples.with.different.packagename.concolic;

import static org.evosuite.symbolic.Assertions.checkEquals;

public class TestCase81 {

	/**
	 * @param args
	 */
	// double double0 = ConcolicMarker.mark(1.5, "double0");
	public static void test(double double0) {
		// box integer
		Double double_instance0 = box(double0);
		// unbox integer
		double double1 = unbox(double_instance0);
		double double2 = 1.5;
		checkEquals(double1, double2);
	}

	public static Double box(Double i) {
		return i;
	}

	public static double unbox(double i) {
		return i;
	}

}
