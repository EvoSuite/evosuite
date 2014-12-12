package com.examples.with.different.packagename.solver;

public class TestCaseRegex {

	public static boolean testConcat(String str) {
		if (str != null && str.matches("a*b")) {
			return true;
		}
		return false;
	}

	public static boolean testUnion(String str) {
		if (str != null && str.matches("a|b")) {
			return true;
		}
		return false;
	}

	public static boolean testOptional(String str) {
		if (str != null && str.matches("(a)?")) {
			return true;
		}
		return false;
	}

	public static boolean testString(String str) {
		if (str != null && str.matches("hello")) {
			return true;
		}
		return false;
	}

	public static boolean testAnyChar(String str) {
		if (str != null && str.matches(".")) {
			return true;
		}
		return false;
	}

	public static boolean testEmpty(String str) {
		if (str != null && str.matches("")) {
			return true;
		}
		return false;
	}

	public static boolean testCross(String str) {
		if (str != null && str.matches("a+")) {
			return true;
		}
		return false;
	}

	public static boolean testRepeatMin(String str) {
		if (str != null && str.matches("a{3,}")) {
			return true;
		}
		return false;
	}

	public static boolean testRepeatMinMax(String str) {
		if (str != null && str.matches("a{3,5}")) {
			return true;
		}
		return false;
	}
	
	public static boolean testRepeatN(String str) {
		if (str != null && str.matches("a{5}")) {
			return true;
		}
		return false;
	}
}
