package com.examples.with.different.packagename.concolic;


import static org.evosuite.symbolic.Assertions.checkEquals;

public class TestCase37 {

	/**
	 * @param args
	 */
	public static void test(int int0) {
		int[] intArray0 = new int[15];
		intArray0[12] = int0;
		int int1 = intArray0[12];
		int int2 = Integer.MAX_VALUE;
		checkEquals(int2,int1);
	}
}
