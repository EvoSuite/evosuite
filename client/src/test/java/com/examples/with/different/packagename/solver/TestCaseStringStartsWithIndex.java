package com.examples.with.different.packagename.solver;

public class TestCaseStringStartsWithIndex {

	public static boolean test(String str) {
		if (str != null) {
			if (str.startsWith("Hello", 5)) {
				return true;
			}
		}
		return true;
	}
}
