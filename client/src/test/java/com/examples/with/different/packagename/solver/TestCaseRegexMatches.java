package com.examples.with.different.packagename.solver;

public class TestCaseRegexMatches {

	public static boolean test(String str) {
		if (str.matches("a*b")) {
			return true;
		}
		return false;
	}

}
