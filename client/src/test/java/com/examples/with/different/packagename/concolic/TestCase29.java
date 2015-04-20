package com.examples.with.different.packagename.concolic;

import static org.evosuite.symbolic.Assertions.checkEquals;

public class TestCase29 {

	/**
	 * @param args
	 */
	public static void test(boolean boolean0, boolean boolean2) {

		{
			boolean boolean1 = true;
			checkEquals(boolean0, boolean1);
		}
		{
			boolean boolean3 = false;
			checkEquals(boolean2, boolean3);
		}

	}

}
