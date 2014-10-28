package com.examples.with.different.packagename.solver;

public class TestCaseSin {

	public static boolean test(double x, double y) {
		double sin_y = Math.sin(y);
		if (x == sin_y) {
			return true;
		} else {
			return false;
		}
	}

}
