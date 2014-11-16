package com.examples.with.different.packagename.solver;

public class TestCaseStringIndexOfString {

	public static boolean test(String str) {
		if (str != null) {
			if (str.indexOf("H") == -1) {
				return true;
			}
		}
		return true;
	}
}
