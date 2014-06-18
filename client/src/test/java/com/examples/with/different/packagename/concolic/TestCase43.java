package com.examples.with.different.packagename.concolic;

import static org.evosuite.symbolic.Assertions.checkEquals;

public class TestCase43 {

	public static void test(int int0) {
		Integer integer0 = new Integer(int0);
		int int1 = integer0.intValue();
		int int2 = Integer.MAX_VALUE;
		checkEquals(int1, int2);

	}
}
