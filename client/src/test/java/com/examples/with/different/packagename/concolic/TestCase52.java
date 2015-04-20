package com.examples.with.different.packagename.concolic;

public class TestCase52 {

	/**
	 * @param args
	 */
	public static void test(String string0, String string1) {

		String[] stringArray0 = string0.split(string1);

		if (stringArray0.length != 3) {
			throw new RuntimeException();
		}
	}

}
