package com.examples.with.different.packagename.concolic;

import static org.evosuite.symbolic.Assertions.checkEquals;


public class TestCase16 {

	public static void test(int int0, int int1, long long0, long long1,
			float float0, float float1, double double0, double double1) {
		{
			// test abs(int,int)
			int int2 = Math.abs(int0);
			checkEquals(int1, int2);
		}
		{
			// test abs(long,long)
			long long2 = Math.abs(long0);
			checkEquals(long1, long2);
		}
		{
			// test abs(float,float)
			float float2 = Math.abs(float0);
			checkEquals(float1, float2);
		}
		{
			// test abs(double,double)
			double double2 = Math.abs(double0);
			checkEquals(double1, double2);
		}
	}

}
