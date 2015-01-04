package com.examples.with.different.packagename.solver;

public class TestCaseStringContains {

	public static boolean test(String str) {
		if (str != null) {
			if (str.contains("Hello") && !str.equals("Hello")) {
				return true;
			}
		}
		return true;
	}
}
