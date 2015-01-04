package com.examples.with.different.packagename.concolic;

import java.util.regex.PatternSyntaxException;

import static org.evosuite.symbolic.Assertions.checkEquals;

public class TestCase66 {

	public static void test(String string1) {
		String string0 = "Togliere sta roba";

		int catchCount = 0;

		try {
			string1.startsWith(null, 0);
		} catch (NullPointerException ex) {
			catchCount++;
		}

		try {
			string1.startsWith("Tog", -1);
		} catch (StringIndexOutOfBoundsException ex) {
			catchCount++;
		}

		try {
			string1.startsWith("Tog", Integer.MAX_VALUE);
		} catch (StringIndexOutOfBoundsException ex) {
			catchCount++;
		}

		checkEquals(1, catchCount);

		boolean boolean0 = string1.startsWith("Tog", 0);
		boolean boolean1 = string0.startsWith("Tog", 0);

		checkEquals(boolean1, boolean0);

	}
}
