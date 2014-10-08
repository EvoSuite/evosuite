package com.examples.with.different.packagename.solver;

public class TestCaseStringIndexOfStringInt {

	public static boolean test(String str) {
		if (str != null) {
			if (str.indexOf("H", 5) == -1) {
				return true;
			}
		}
		return true;
	}
}
