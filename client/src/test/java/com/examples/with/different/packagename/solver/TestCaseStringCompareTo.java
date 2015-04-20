package com.examples.with.different.packagename.solver;

public class TestCaseStringCompareTo {

	public static boolean test(String str) {
		if (str != null) {
			if (str.compareTo("Hello") == 0) {
				return true;
			}
		}
		return true;
	}
}
