package com.examples.with.different.packagename.solver;

public class TestCaseStringConcat {

	public static boolean test(String str) {
		if (str != null) {
			String concat = str.concat("ppy");
			if (concat.equals("happy")) {
				return true;
			}
		}
		return true;
	}
}
