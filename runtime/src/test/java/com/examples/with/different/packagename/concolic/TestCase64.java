package com.examples.with.different.packagename.concolic;

import static org.evosuite.symbolic.Assertions.checkEquals;

public class TestCase64 {

	public static void test(String string1) {
		String string0 = "Togliere sta roba";

		int catchCount = 0;

		try {
			string1.concat(null);
		} catch (NullPointerException ex) {
			catchCount++;
		}

		checkEquals(1, catchCount);

		String string2 = string1.concat(string0);
		String string3 = "Togliere sta roba" + "Togliere sta roba";
		boolean boolean0 = string2.equals(string3);

		checkEquals(true, boolean0);
	}
}
