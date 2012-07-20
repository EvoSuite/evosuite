package org.evosuite.symbolic;

import static org.evosuite.symbolic.Assertions.checkEquals;

import org.evosuite.symbolic.dsc.ConcolicMarker;

public class TestCase19 {

	private static final float FLOAT_VALUE = (float) Math.E;
	
	private static final double DOUBLE_VALUE = Math.PI;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		{
			// test getExponent(float,float)
			float float0 = ConcolicMarker.mark(FLOAT_VALUE, "float0");
			float float1 = FLOAT_VALUE;
			int int0 = Math.round(float0);
			int int1 = Math.round(float1);
			checkEquals(int0, int1);
		}
		{
			// test getExponent(double,double)
			double double0 = ConcolicMarker.mark(DOUBLE_VALUE, "double0");
			double double1 = DOUBLE_VALUE;
			long long0 = Math.round(double0);
			long long1 = Math.round(double1);
			checkEquals(long0, long1);
		}
	}

}
