package com.examples.with.different.packagename.solver;

public class TestCaseCastStringToInt {

	public static boolean test(String string0) {
		int int0 = Integer.parseInt(string0);
		if (int0 == 0) {
			return true;
		} else {
			return false;
		}
	}

}
