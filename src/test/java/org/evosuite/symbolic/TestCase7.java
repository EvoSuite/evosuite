package org.evosuite.symbolic;

import org.evosuite.symbolic.dsc.ConcolicMarker;

public class TestCase7 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MathLong mathLong0 = new MathLong();
		mathLong0.unreach();
		MathLong mathLong1 = new MathLong();
		long long0 = ConcolicMarker.mark(0L,"var1");
		long long1 = ConcolicMarker.mark(0L, "var2");
		char char0 = mathLong1.castToChar(long0);
		long long2 = mathLong1.divide(long0, long1);
	}

}
