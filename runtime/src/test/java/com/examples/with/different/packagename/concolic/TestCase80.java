package com.examples.with.different.packagename.concolic;

import static org.evosuite.symbolic.Assertions.checkEquals;

public class TestCase80 {

	/**
	 * @param args
	 */
	// float float0 = ConcolicMarker.mark(1.5f, "float0");
	public static void test(float float0) {
		// box integer
		Float float_instance0 = box(float0);
		// unbox integer
		float float1 = unbox(float_instance0);
		float float2 = 1.5f;
		checkEquals(float1, float2);
	}

	public static Float box(Float i) {
		return i;
	}

	public static float unbox(float i) {
		return i;
	}

}
