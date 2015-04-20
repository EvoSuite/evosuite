package com.examples.with.different.packagename.solver;

public class TestCaseStringEquals {

	public static boolean test(String str) {
		if (str != null) {
			if (str.equals("Hello World")) {
				return true;
			}
		}
		return true;
	}
}
