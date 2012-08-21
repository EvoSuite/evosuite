package org.evosuite.symbolic;
import static org.evosuite.symbolic.Assertions.checkEquals;

import org.evosuite.symbolic.dsc.ConcolicMarker;


public class TestCase18 {


	private static final float FLOAT_VALUE = -0.0099100191F;

	private static final double DOUBLE_VALUE = Math.PI;

	public static void main(String[] args) {

		{
			// test getExponent(float,float)
			float float0 = ConcolicMarker.mark(FLOAT_VALUE, "float0");
			float float1 = FLOAT_VALUE;
			float float2 = Math.nextUp(float0);
			float float3 = Math.nextUp(float1);
			checkEquals(float2, float3);
		}
		{
			// test getExponent(double,double)
			double double0 = ConcolicMarker.mark(DOUBLE_VALUE, "double0");
			double double1 = DOUBLE_VALUE;
			double double2 = Math.nextUp(double0);
			double double3 = Math.nextUp(double1);
			checkEquals(double2, double3);
		}
	}
}
