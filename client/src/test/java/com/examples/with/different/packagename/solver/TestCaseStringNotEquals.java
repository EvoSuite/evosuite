package com.examples.with.different.packagename.solver;

public class TestCaseStringNotEquals {

	public static boolean test(String str) {
		if (str != null) {
			if (str.equals("Hello World")) {
				return true;
			}
		}
		return true;
	}
}
