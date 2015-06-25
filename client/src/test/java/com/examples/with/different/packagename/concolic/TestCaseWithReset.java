package com.examples.with.different.packagename.concolic;

public class TestCaseWithReset {

	private static int count = 0;

	public static void inc() {
		count++;
	}

	public static boolean isZero(int value) {
		if (count == 0) {
			if (value == 0) {
				return true;
			} else {
				return false;
			}
		} else
			return false;
	}

}
