package org.evosuite.symbolic;

import org.evosuite.symbolic.dsc.ConcolicMarker;

public class TestCase12 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MathLong mathlong0 = new MathLong();
		long long0 = ConcolicMarker.mark(1,"var1");
		long long1 = ConcolicMarker.mark(4,"var2");
		long long2 = mathlong0.shiftLeft(long0, long1);
		long long3 = ConcolicMarker.mark(16,"var3");
		if (long2!=long3) {
			mathlong0.castToChar(long3);
		}
		long long4 = mathlong0.shiftRight(long0, long1);
		long long5 = ConcolicMarker.mark(0, "var4");
		if (long4!=long5) {
			mathlong0.castToChar(long3);
		}
		long long6 = mathlong0.unsignedShiftRight(long0, long1);
		if (long6!=long5) {
			mathlong0.castToChar(long3);
		}
	}

}
