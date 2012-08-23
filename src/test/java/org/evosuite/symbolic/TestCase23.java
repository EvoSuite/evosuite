package org.evosuite.symbolic;

import static org.evosuite.symbolic.Assertions.checkEquals;

public class TestCase23 {

	static final String STRING_VALUE = "Togliere sta roba";

	/**
	 * @param args
	 */
	public static void test(String string0) {

		String string1 = STRING_VALUE;
		int int0 = string0.length();
		int int1 = string1.length();
		checkEquals(int0, int1);

	}

}
