package com.examples.with.different.packagename.concolic;

import static org.evosuite.symbolic.Assertions.checkEquals;

public class TestCase24 {

	public static final String STRING_VALUE = "Togliere sta roba";

	/**
	 * @param args
	 */
	public static void test(String string0) {

		String string1 = STRING_VALUE;
		{
			String string2 = string0.trim();
			String string3 = string1.trim();
			int int0 = string2.length();
			int int1 = string3.length();
			checkEquals(int0, int1);
		}
		{
			String string2 = string0.toLowerCase();
			String string3 = string1.toLowerCase();
			int int0 = string2.length();
			int int1 = string3.length();
			checkEquals(int0, int1);
		}
		{
			String string2 = string0.toUpperCase();
			String string3 = string1.toUpperCase();
			int int0 = string2.length();
			int int1 = string3.length();
			checkEquals(int0, int1);
		}

	}

}
