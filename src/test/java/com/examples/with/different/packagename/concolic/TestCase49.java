package com.examples.with.different.packagename.concolic;

public class TestCase49 {

	/**
	 * @param args
	 */
	public static void test() {

		String[] stringArray = null;
		stringArray = new String[10];
		stringArray[1] = "Togliere";

		if (stringArray[1] == null) {
			throw new RuntimeException();
		}

		if (stringArray[0] != null) {
			throw new RuntimeException();
		}

		TestCase49[] anotherArray = null;
		anotherArray = new TestCase49[10];
		anotherArray[0] = new TestCase49();

		if (anotherArray[0] == null) {
			throw new RuntimeException();
		}

	}

}
