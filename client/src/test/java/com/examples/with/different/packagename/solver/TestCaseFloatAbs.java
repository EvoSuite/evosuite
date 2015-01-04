package com.examples.with.different.packagename.solver;

public class TestCaseFloatAbs {

	public static boolean test(double x) {
		double abs_x = Math.abs(x);
		if (abs_x > 0) {
			return true;
		} else {
			return false;
		}
	}
}
