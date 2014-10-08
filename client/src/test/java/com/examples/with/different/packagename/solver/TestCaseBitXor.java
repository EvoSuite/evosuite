package com.examples.with.different.packagename.solver;

public class TestCaseBitXor {

	public static boolean test(int x, int y) {
		if (x == (y ^ 1)) {
			return true;
		} else {
			return false;
		}
	}

}
