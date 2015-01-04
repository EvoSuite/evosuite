package com.examples.with.different.packagename.solver;

public class TestCaseAbs {

	public static boolean test(int x) {
		int abs_x = Math.abs(x);
		if (abs_x>0) {
			return true;
		} else {
			return false;
		}
	}
}
