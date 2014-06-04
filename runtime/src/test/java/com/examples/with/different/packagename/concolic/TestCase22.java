package com.examples.with.different.packagename.concolic;

import static org.evosuite.symbolic.Assertions.checkEquals;

public class TestCase22 {

	public static final float FLOAT_VALUE_1 = 0.0099100191F;
	private static final float FLOAT_VALUE_2 = -0.3333F;

	public static final double DOUBLE_VALUE_1 = Math.PI;
	private static final double DOUBLE_VALUE_2 = -20220020D;

	public static void test(float float0, double double0) {

		{
			// test getExponent(float,float)
			float float1 = FLOAT_VALUE_1;
			float float2 = FLOAT_VALUE_2;
			float float3 = Math.copySign(float0, float2);
			float float4 = Math.copySign(float1, float2);
			checkEquals(float3, float4);
		}
		{
			// test getExponent(double,double)
			double double1 = DOUBLE_VALUE_1;
			double double2 = DOUBLE_VALUE_2;
			double double3 = Math.copySign(double0, double2);
			double double4 = Math.copySign(double1, double2);
			checkEquals(double3, double4);
		}
	}
}
