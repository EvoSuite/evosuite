package com.examples.with.different.packagename.solver;

public class TestCaseAcos {

	public static boolean test(double x, double y) {
		double acos_y = Math.acos(y);
		if (x == acos_y) {
			return true;
		} else {
			return false;
		}
	}

}
