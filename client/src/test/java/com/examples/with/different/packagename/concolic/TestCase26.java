package com.examples.with.different.packagename.concolic;

import static org.evosuite.symbolic.Assertions.checkEquals;

public class TestCase26 {

	public static final String STRING_VALUE_PART_1 = "Togliere sta";

	private static final String STRING_VALUE_PART_2 = " roba";

	/**
	 * @param args
	 */
	public static void test(String string0) {
		String string1 = STRING_VALUE_PART_1;
		String string2 = string0.concat(STRING_VALUE_PART_2);
		String string3 = string1.concat(STRING_VALUE_PART_2);
		int int0 = string2.length();
		int int1 = string3.length();
		checkEquals(int0, int1);
	}

}
