package com.examples.with.different.packagename.concolic;

import static org.evosuite.symbolic.Assertions.checkEquals;

public class TestCase58 {

	/**
	 * @param args
	 */
//	String string1 = ConcolicMarker.mark("Togliere sta roba".toUpperCase(),
//			"string1");
	public static void test(String string1) {

		String string0 = "Togliere sta roba";

		int int0 = string0.toUpperCase().length();
		int int1 = string1.toUpperCase().length();

		checkEquals(int0, int1);

		String string2 = string0.toLowerCase().trim();
		String string3 = string1.toLowerCase().trim();

		int int2 = string2.toUpperCase().length();
		int int3 = string3.toUpperCase().length();

		checkEquals(int2, int3);
	}
}
