package com.examples.with.different.packagename.concolic;

import static org.evosuite.symbolic.Assertions.checkEquals;

public class TestCase32 {

	public static final String STRING_VALUE = "Togliere sta roba";

	private static final CharSequence CHAR_SEQUENCE = new StringBuffer("ere");

	private static final CharSequence NEW_CHAR_SEQUENCE = new StringBuffer("q");

	public static void test(String string0) {

		String string1 = STRING_VALUE;

		// The StringBuffer CharSequence element makes this constraint concrete
		{
			boolean boolean0 = string0.contains(CHAR_SEQUENCE);
			boolean boolean1 = string1.contains(CHAR_SEQUENCE);
			checkEquals(boolean0, boolean1);
		}

		// The StringBuffer CharSequence element makes this constraint concrete
		{
			String string2 = string0.replace(CHAR_SEQUENCE, NEW_CHAR_SEQUENCE);
			String string3 = string1.replace(CHAR_SEQUENCE, NEW_CHAR_SEQUENCE);
			int int0 = string2.length();
			int int1 = string3.length();
			checkEquals(int0, int1);
		}
	}
}
