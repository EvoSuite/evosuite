package com.examples.with.different.packagename;

public class RegexBranch {

	public static boolean foo(String x) {
		if (x.matches("[A-Z][a-z][0-9]")) {
			return true;
		} else {
			return false;
		}
	}
}
