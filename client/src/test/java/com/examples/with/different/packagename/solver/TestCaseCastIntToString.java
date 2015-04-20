package com.examples.with.different.packagename.solver;

public class TestCaseCastIntToString {
	
	public static boolean test(int int0) {
		String string0 = String.valueOf(int0);
		if (string0.equals("0")) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean test(long long0) {
		String string0 = String.valueOf(long0);
		if (string0.equals("0")) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean test(char char0) {
		String string0 = String.valueOf(char0);
		if (string0.equals("0")) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean test(boolean boolean0) {
		String string0 = String.valueOf(boolean0);
		if (string0.equals("false")) {
			return true;
		} else {
			return false;
		}
	}

}
