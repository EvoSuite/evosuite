package org.evosuite.symbolic.search;

import org.evosuite.symbolic.Assertions;

public class TestInput1 {

	/**
	 * 
	 * @param int0
	 *            ==-15
	 * @param long0
	 *            ==Long.MAX_VALUE
	 * @param string0
	 *            .equals("Togliere sta roba")
	 */
	public static void test(int int0, long long0, String string0) {
		int int1 = Math.abs(int0);
		int int2 = (int) Math.min(int1, long0);
		int int3 = string0.length();

		int int4 = (int) Math.min(Math.abs(-15), Long.MAX_VALUE);
		Assertions.checkEquals(int4, int2);

		int int5 = new String("Togliere sta roba").length();
		Assertions.checkEquals(int5, int3);

	}
}
