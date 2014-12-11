package com.examples.with.different.packagename.solver;

public class TestCaseStringAppendInteger {

	public static boolean test(String str) {
		if (str != null) {
			String concat = str + 10;
			if (concat.equals("ha10")) {
				return true;
			}
		}
		return true;
	}
}
