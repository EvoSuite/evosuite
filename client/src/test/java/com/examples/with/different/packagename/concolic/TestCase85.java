package com.examples.with.different.packagename.concolic;

import static org.evosuite.symbolic.Assertions.checkEquals;

public class TestCase85 {

	/**
	 * @param args
	 */
	// String string0 = ConcolicMarker.mark("aaaaaaaaaaab", "string0");
	public static void test(String string0) {

		String regex = "a*b";
		boolean boolean0 = string0.matches(regex);
		checkEquals(boolean0, true);
	}

}
