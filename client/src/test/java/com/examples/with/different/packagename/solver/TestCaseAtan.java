package com.examples.with.different.packagename.solver;

public class TestCaseAtan {

	public static boolean test(double x, double y) {
		double atan_y = Math.atan(y);
		if (x == atan_y) {
			return true;
		} else {
			return false;
		}
	}

}
