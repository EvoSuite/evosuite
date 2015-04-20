package com.examples.with.different.packagename.solver;

public class TestCaseStringSubstringFromTo {

	public static boolean test(String str) {
		if (str != null && str.length() == "hamburger".length()) {
			String substr = str.substring(4, 8);
			if (substr.equals("urge")) {
				return true;
			}
		}
		return true;
	}
}
