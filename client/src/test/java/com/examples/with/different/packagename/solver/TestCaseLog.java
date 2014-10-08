package com.examples.with.different.packagename.solver;

public class TestCaseLog {

	public static boolean test(double x, double y) {
		double log_y = Math.log(y);
		if (x == log_y) {
			return true;
		} else {
			return false;
		}
	}

}
