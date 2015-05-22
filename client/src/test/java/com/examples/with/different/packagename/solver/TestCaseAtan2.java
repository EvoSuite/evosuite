package com.examples.with.different.packagename.solver;

public class TestCaseAtan2 {

	public static boolean test(double x, double y, double z) {
		double atan_2 = Math.atan2(y, z);
		if (x == atan_2) {
			return true;
		} else {
			return false;
		}
	}

}
