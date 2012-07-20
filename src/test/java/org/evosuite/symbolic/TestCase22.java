package org.evosuite.symbolic;
import static org.evosuite.symbolic.Assertions.checkEquals;

import org.evosuite.symbolic.dsc.ConcolicMarker;


public class TestCase22 {


	private static final float FLOAT_VALUE_1 = 0.0099100191F;
	private static final float FLOAT_VALUE_2 = -0.3333F;

	private static final double DOUBLE_VALUE_1 = Math.PI;
	private static final double DOUBLE_VALUE_2 = -20220020D;


	public static void main(String[] args) {

		{
			// test getExponent(float,float)
			float float0 = ConcolicMarker.mark(FLOAT_VALUE_1, "float0");
			float float1 = FLOAT_VALUE_1;
			float float2 = FLOAT_VALUE_2;
			float float3 = Math.copySign(float0,float2);
			float float4 = Math.copySign(float1,float2);
			checkEquals(float3, float4);
		}
		{
			// test getExponent(double,double)
			double double0 = ConcolicMarker.mark(DOUBLE_VALUE_1, "double0");
			double double1 = DOUBLE_VALUE_1;
			double double2 = DOUBLE_VALUE_2;
			double double3 = Math.copySign(double0,double2);
			double double4 = Math.copySign(double1,double2);
			checkEquals(double3, double4);
		}
	}
}
