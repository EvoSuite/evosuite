package com.examples.with.different.packagename.solver;

public class TestCaseStringLastIndexOfChar {

	public static boolean test(String str) {
		if (str != null) {
			if (str.lastIndexOf('H') == -1) {
				return true;
			}
		}
		return true;
	}
}
