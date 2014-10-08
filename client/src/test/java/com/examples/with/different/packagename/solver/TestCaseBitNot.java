package com.examples.with.different.packagename.solver;

public class TestCaseBitNot {

	public static boolean test(int x, int y) {
		if (y==10 && x == (~y)) {
			return true;
		} else {
			return false;
		}
	}

}
