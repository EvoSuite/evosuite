package com.examples.with.different.packagename.solver;

public class TestCaseStringLowerCase {

	public static boolean test(String str) {
		if (str != null && str.length() > 0) {
			String lowerCase = str.toLowerCase();
			if (!lowerCase.equals(str)) {
				return true;
			}
		}
		return true;
	}
}
