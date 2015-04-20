package com.examples.with.different.packagename.solver;

public class TestCaseTan {

	public static boolean test(double x, double y) {
		double tan_y = Math.tan(y);
		if (x == tan_y) {
			return true;
		} else {
			return false;
		}
	}

}
