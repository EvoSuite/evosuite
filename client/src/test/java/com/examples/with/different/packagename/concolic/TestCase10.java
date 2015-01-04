package com.examples.with.different.packagename.concolic;

import org.evosuite.symbolic.MathDouble;

public class TestCase10 {

	/**
	 * @param args
	 */
	public static void test(double double0, double double1, double double3,
			double double4) {

		MathDouble mathDouble0 = new MathDouble();
		double double2 = (double) mathDouble0.castToLong(double0);
		mathDouble0.unreach();
		int int0 = mathDouble0.castToInt(double3);
		char char0 = mathDouble0.castToChar(double3);
		long long0 = mathDouble0.castToLong(double4);
		double double5 = mathDouble0.substract((double) int0, (double) int0);
		if (double5 == double4) {
			mathDouble0.castToFloat(double5);
		}

	}

}
