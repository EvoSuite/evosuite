package com.examples.with.different.packagename.solver;

public class TestCaseStringIndexOfCharInt {

	public static boolean test(String str) {
		if (str != null) {
			if (str.indexOf('H', 5) == -1) {
				return true;
			}
		}
		return true;
	}
}
