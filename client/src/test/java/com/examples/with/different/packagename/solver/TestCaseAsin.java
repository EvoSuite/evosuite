package com.examples.with.different.packagename.solver;

public class TestCaseAsin {

	public static boolean test(double x, double y) {
		double asin_y = Math.asin(y);
		if (x == asin_y) {
			return true;
		} else {
			return false;
		}
	}

}
