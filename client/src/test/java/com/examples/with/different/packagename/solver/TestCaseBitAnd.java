package com.examples.with.different.packagename.solver;

public class TestCaseBitAnd {

	public static boolean test(int x, int y) {
		if (y==10 && x == (y & 1)) {
			return true;
		} else {
			return false;
		}
	}

}
