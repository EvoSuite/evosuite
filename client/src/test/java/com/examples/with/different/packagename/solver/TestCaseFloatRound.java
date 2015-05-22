package com.examples.with.different.packagename.solver;

public class TestCaseFloatRound {

	public static boolean test(double x, int y) {
		if (Math.round(x) == y) {
			return true;
		} else {
			return false;
		}
	}

}
