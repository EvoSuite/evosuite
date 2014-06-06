package com.examples.with.different.packagename.concolic;

import static org.evosuite.symbolic.Assertions.checkEquals;

public class TestCase57 {

	/**
	 * @param args
	 */
	// String string1 = ConcolicMarker.mark("Togliere sta roba".toUpperCase(),
	// "string1");
	// String string3 = ConcolicMarker.mark("Togliere", "string3");
	public static void test(String string1, String string3) {

		String string0 = "Togliere sta roba";

		int int0 = string0.length();
		int int1 = string1.length();

		checkEquals(int0, int1);

		try {
			String string2 = null;
			int int2 = string2.length();
		} catch (NullPointerException ex) {
			System.out.println("Hello world!");
		}

		int int3 = string3.length();

		checkEquals(int1, int3);
	}
}
