package com.examples.with.different.packagename.solver;

public class TestCaseRound {

	public static boolean test(int x, double y) {
		int round_y = (int) Math.round(y);
		if (x == round_y) {
			return true;
		} else {
			return false;
		}
	}

}
