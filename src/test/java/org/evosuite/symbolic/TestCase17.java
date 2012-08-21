package org.evosuite.symbolic;

import static org.evosuite.symbolic.Assertions.checkEquals;

import org.evosuite.symbolic.dsc.ConcolicMarker;

public class TestCase17 {

	private static final float FLOAT_VALUE = -0.0099100191F;

	private static final double DOUBLE_VALUE = Math.PI;

	public static void main(String[] args) {

		{
			// test getExponent(float,float)
			float float0 = ConcolicMarker.mark(FLOAT_VALUE, "float0");
			float float1 = FLOAT_VALUE;
			int int0 = Math.getExponent(float0);
			int int1 = Math.getExponent(float1);
			checkEquals(int0, int1);
		}
		{
			// test getExponent(double,double)
			double double0 = ConcolicMarker.mark(DOUBLE_VALUE, "double0");
			double double1 = DOUBLE_VALUE;
			int int2 = Math.getExponent(double0);
			int int3 = Math.getExponent(double1);
			checkEquals(int2, int3);
		}
	}

}
