package com.examples.with.different.packagename.solver;

public class TestCaseBinaryOp {

	public static boolean testAdd(int x, int y) {
		int z = x + y;
		if (x == z) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean testSub(int x, int y) {
		int z = y - 10;
		if (x == z) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean testMul(int x, int y) {
		if (x != 0 && y == x * 2) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean testMul2(int x, int y) {
		if (10 == x * y) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean testDiv(int x, int y) {
		if (x == y / 5) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean testMod(int x, int y) {
		int z = x % y;
		if (x == z) {
			return true;
		} else {
			return false;
		}
	}

}
