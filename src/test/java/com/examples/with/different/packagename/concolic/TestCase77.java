package com.examples.with.different.packagename.concolic;

import static org.evosuite.symbolic.Assertions.checkEquals;

public class TestCase77 {

	/**
	 * @param args
	 */
	// int int0 = ConcolicMarker.mark(10,"int0");
	// int int1 = ConcolicMarker.mark(20,"int1");
	// int int2 = ConcolicMarker.mark(30,"int2");
	public static void test(int int0, int int1, int int2) {
		int[][][] multiArray = new int[int0][int1][int2];
		int int3 = multiArray.length;
		int int4 = multiArray[0].length;
		int int5 = multiArray[0][0].length;
		checkEquals(int3, 10);
		checkEquals(int4, 20);
		checkEquals(int5, 30);
	}

}
