package com.examples.with.different.packagename.concolic;

import static org.evosuite.symbolic.Assertions.checkEquals;

public class TestCase40 {

	/**
	 * @param args
	 */
	public static void test(int int0, int int1, float float0) {

		final int ARRAY_SIZE = 10;

		float[] floatArray0 = new float[int0];
		floatArray0[floatArray0.length - 1] = float0;
		float float1 = floatArray0[((int0 - int1) * 2 / 2)];

		checkEquals(float0, float1);

	}
}
