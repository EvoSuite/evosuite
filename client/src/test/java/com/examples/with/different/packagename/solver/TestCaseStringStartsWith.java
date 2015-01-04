package com.examples.with.different.packagename.solver;

public class TestCaseStringStartsWith {

	public static boolean test(String str) {
		if (str != null) {
			if (str.startsWith("Hello") && !str.equals("Hello")) {
				return true;
			}
		}
		return true;
	}
}
