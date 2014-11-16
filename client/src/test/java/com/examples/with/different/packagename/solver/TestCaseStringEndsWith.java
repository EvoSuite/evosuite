package com.examples.with.different.packagename.solver;

public class TestCaseStringEndsWith {

	public static boolean test(String str) {
		if (str != null) {
			if (str.endsWith("World") && str.startsWith("Hello")) {
				return true;
			}
		}
		return true;
	}
}
