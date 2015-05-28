package com.examples.with.different.packagename.solver;

public class TestCaseFloatSymbolicMod {

	public static boolean test(double x, double y) {
		if ((x != 0) && (x == (2.2 % 2.0))) {
			return true;
		} else {
			return false;
		}
	}

}
