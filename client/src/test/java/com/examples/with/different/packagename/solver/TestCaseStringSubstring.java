package com.examples.with.different.packagename.solver;

public class TestCaseStringSubstring {

	public static boolean test(String str) {
		if (str != null && str.length() == 7) {
			String substr = str.substring(2);
			if (substr.equals("happy")) {
				return true;
			}
		}
		return true;
	}
}
