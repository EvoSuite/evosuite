package com.examples.with.different.packagename.solver;

public class TestCaseStringAppendString {

	public static boolean test(String str) {
		if (str != null) {
			String concat = str + "ppy";
			if (concat.equals("happy")) {
				return true;
			}
		}
		return true;
	}
}
