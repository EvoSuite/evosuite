package com.examples.with.different.packagename.solver;

public class TestCaseStringEqualsIgnoreCase {

	public static boolean test(String str) {
		final String str2 = "bar";
		if (!str.equals(str2)) {
			if (str.equalsIgnoreCase(str2)) {
				return true;
			}
		}
		return false;
	}

}
