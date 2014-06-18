package com.examples.with.different.packagename.concolic;

import static org.evosuite.symbolic.Assertions.checkEquals;

public class TestCase79 {

	/**
	 * @param args
	 */
	public static void test(long long0) {
		// box integer
		Long long_instance0 = box(long0);
		// unbox integer
		long long1 = unbox(long_instance0);
		long long2 = 10L;
		checkEquals(long1, long2);
	}

	public static Long box(Long i) {
		return i;
	}

	public static long unbox(long i) {
		return i;
	}

}
