package com.examples.with.different.packagename.solver;

public class TestCaseStringCharAt {

	public static boolean test(String str) {
		if (str != null && str.length() > 0) {
			if (str.charAt(0) == 'X') {
				return true;
			}
		}
		return true;
	}
}
