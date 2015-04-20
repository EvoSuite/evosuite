package com.examples.with.different.packagename.concolic;

import static org.evosuite.symbolic.Assertions.checkEquals;


public class TestCase19 {

	public static final float FLOAT_VALUE = (float) Math.E;

	public static final double DOUBLE_VALUE = Math.PI;

	/**
	 * @param args
	 */
	public static void test(float float0, double double0) {
		{
			// test getExponent(float,float)
			float float1 = FLOAT_VALUE;
			int int0 = Math.round(float0);
			int int1 = Math.round(float1);
			checkEquals(int0, int1);
		}
		{
			// test getExponent(double,double)
			double double1 = DOUBLE_VALUE;
			long long0 = Math.round(double0);
			long long1 = Math.round(double1);
			checkEquals(long0, long1);
		}
	}

}
