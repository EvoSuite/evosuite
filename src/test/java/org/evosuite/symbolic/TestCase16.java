package org.evosuite.symbolic;

import static org.evosuite.symbolic.Assertions.checkEquals;

import org.evosuite.symbolic.dsc.ConcolicMarker;

public class TestCase16 {

	public static void main(String[] args) {
		{
			// test abs(int,int)
			int int0 = ConcolicMarker.mark(-99100191, "int0");
			int int1 = ConcolicMarker.mark(99100191, "int1");
			int int2 = Math.abs(int0);
			checkEquals(int1, int2);
		}
		{
			// test abs(long,long)
			long long0 = ConcolicMarker.mark(-991001911414177541L, "long0");
			long long1 = ConcolicMarker.mark(991001911414177541L, "long1");
			long long2 = Math.abs(long0);
			checkEquals(long1, long2);
		}
		{
			// test abs(float,float)
			float float0 = ConcolicMarker.mark(-0.0099100191F, "float0");
			float float1 = ConcolicMarker.mark(+0.0099100191F, "float1");
			float float2 = Math.abs(float0);
			checkEquals(float1, float2);
		}
		{
			// test abs(double,double)
			double double0 = ConcolicMarker.mark(-0.0099100191F, "double0");
			double double1 = ConcolicMarker.mark(+0.0099100191F, "double1");
			double double2 = Math.abs(double0);
			checkEquals(double1, double2);
		}
	}


}
