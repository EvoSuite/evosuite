package com.examples.with.different.packagename.solver;

public class TestCaseStringReplaceChar {

	public static boolean test(String str) {
		if (str != null) {
			String replace = str.replace('x', 'y');
			if (replace.equals("happy")) {
				return true;
			}
		}
		return true;
	}
}
