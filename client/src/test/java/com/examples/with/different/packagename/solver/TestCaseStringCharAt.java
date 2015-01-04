package com.examples.with.different.packagename.solver;

public class TestCaseStringCharAt {

	public static boolean test(String str, int intValue) {
		char charValue = (char) intValue;
		if (str != null && str.length() > 5) {
			if (str.charAt(0) == charValue) {
				return true;
			}
		}
		return true;
	}

	public static boolean test(String str) {
		if (str != null && str.length() > 0) {
			if (str.charAt(0) == 'X') {
				return true;
			}
		}
		return true;
	}
	
	public static boolean test3(String str) {
		if (str != null && str.length() > 0) {
			if (str.charAt(0) == '0') {
				return true;
			}
		}
		return true;
	}
}
