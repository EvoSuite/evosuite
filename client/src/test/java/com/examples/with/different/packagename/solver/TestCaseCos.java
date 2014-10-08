package com.examples.with.different.packagename.solver;

public class TestCaseCos {

	public static boolean test(double x, double y) {
		double cos_y = Math.cos(y);
		if (x == cos_y) {
			return true;
		} else {
			return false;
		}
	}

}
