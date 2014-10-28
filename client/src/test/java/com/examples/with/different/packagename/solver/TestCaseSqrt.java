package com.examples.with.different.packagename.solver;

public class TestCaseSqrt {

	public static boolean test(double x, double y) {
		double sqrt_y = Math.sqrt(y);
		if (x == sqrt_y) {
			return true;
		} else {
			return false;
		}
	}

}
