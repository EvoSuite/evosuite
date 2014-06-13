package com.examples.with.different.packagename.concolic;

public class TestCase56 {

	/**
	 * @param args
	 */
	// int int0 = ConcolicMarker.mark(1515, "int0");
	// int int2 = ConcolicMarker.mark(1541, "int2");
	public static void test(int int0, int int2) {
		int int1 = 45451;
		if (int0 == int1) {
			return;
		}
		if (int2 == int0) {
			return;
		}

	}

}
