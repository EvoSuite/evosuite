package com.examples.with.different.packagename.solver;

public class TestCaseCastRealToInt {

	public static boolean test(double double_x) {

		int int_x = (int) double_x;
		if (double_x != 0 && int_x == 0) {
			return true;
		} else {
			return false;
		}

	}

}
