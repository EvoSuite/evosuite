package com.examples.with.different.packagename.solver;

public class TestCaseCastIntToReal {

	public static boolean test(int int_x) {

		double double_x = (double) int_x;
		if (double_x != 0) {
			return true;
		} else {
			return false;
		}

	}

}
