package com.examples.with.different.packagename.concolic;

import static org.evosuite.symbolic.Assertions.checkEquals;

public class TestCase17 {

	public static final float FLOAT_VALUE = -0.0099100191F;

	public static final double DOUBLE_VALUE = Math.PI;

	public static void test(float float0, double double0) {

		{
			// test getExponent(float,float)
			float float1 = FLOAT_VALUE;
			int int0 = Math.getExponent(float0);
			int int1 = Math.getExponent(float1);
			checkEquals(int0, int1);
		}
		{
			// test getExponent(double,double)
			double double1 = DOUBLE_VALUE;
			int int2 = Math.getExponent(double0);
			int int3 = Math.getExponent(double1);
			checkEquals(int2, int3);
		}
	}

}
