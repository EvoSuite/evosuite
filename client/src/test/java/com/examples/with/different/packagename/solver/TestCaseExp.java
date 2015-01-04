package com.examples.with.different.packagename.solver;

public class TestCaseExp {

	public static boolean test(double x, double y) {
		double exp_y = Math.exp(y);
		if (x == exp_y) {
			return true;
		} else {
			return false;
		}
	}

}
