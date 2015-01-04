package com.examples.with.different.packagename.solver;

public class TestCaseStringLength {

	public static boolean test(String str) {
		if (str != null) {
			if (str.length() == 5) {
				return true;
			}
		}
		return true;
	}
}
