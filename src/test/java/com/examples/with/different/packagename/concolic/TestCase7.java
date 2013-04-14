package com.examples.with.different.packagename.concolic;

import org.evosuite.symbolic.MathLong;

public class TestCase7 {

	/**
	 * @param args
	 */
	// long long0 = ConcolicMarker.mark(0L,"var1");
	// long long1 = ConcolicMarker.mark(0L, "var2");
	public static void test(long long0, long long1) {
		MathLong mathLong0 = new MathLong();
		mathLong0.unreach();
		MathLong mathLong1 = new MathLong();
		char char0 = mathLong1.castToChar(long0);
		long long2 = mathLong1.divide(long0, long1);
	}

}
