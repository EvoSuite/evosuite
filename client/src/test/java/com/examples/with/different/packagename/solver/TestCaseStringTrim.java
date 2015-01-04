package com.examples.with.different.packagename.solver;

public class TestCaseStringTrim {

	public static boolean test(String str) {
		if (str != null && str.length() > 0) {
			String trim = str.trim();
			if (trim.length() < str.length()) {
				return true;
			}
		}
		return true;
	}
}
