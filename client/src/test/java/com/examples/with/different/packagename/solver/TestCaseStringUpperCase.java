package com.examples.with.different.packagename.solver;

public class TestCaseStringUpperCase {

	public static boolean test(String str) {
		if (str != null && str.length() > 0) {
			String upperCase = str.toUpperCase();
			if (!upperCase.equals(str)) {
				return true;
			}
		}
		return true;
	}
}
