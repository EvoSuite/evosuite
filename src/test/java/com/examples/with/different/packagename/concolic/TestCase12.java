package com.examples.with.different.packagename.concolic;

import org.evosuite.symbolic.MathLong;

public class TestCase12 {

	/**
	 * @param args
	 */
	public static void test(long long0, long long1, long long3, long long5) {

		MathLong mathlong0 = new MathLong();
		long long2 = mathlong0.shiftLeft(long0, long1);
		if (long2 != long3) {
			mathlong0.castToChar(long3);
		}
		long long4 = mathlong0.shiftRight(long0, long1);
		if (long4 != long5) {
			mathlong0.castToChar(long3);
		}
		long long6 = mathlong0.unsignedShiftRight(long0, long1);
		if (long6 != long5) {
			mathlong0.castToChar(long3);
		}
	}

}
