package com.examples.with.different.packagename.concolic;

import static org.evosuite.symbolic.Assertions.checkEquals;

public class TestCase30 {

	public static final String STRING_VALUE = "Togliere sta roba";

	private static final Object SOME_OBJECT = new Object();

	public static void test(String string0) {

		String string1 = STRING_VALUE;
		{
			boolean boolean0 = string0.equals(SOME_OBJECT);
			boolean boolean1 = string1.equals(SOME_OBJECT);
			checkEquals(boolean0, boolean1);
		}
	}
}
