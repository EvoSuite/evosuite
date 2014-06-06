package com.examples.with.different.packagename.concolic;

import static org.evosuite.symbolic.Assertions.checkEquals;

public class TestCase59 {

	/**
	 * @param args
	 */
//	String string1 = ConcolicMarker.mark(string0, "string1");
//	int int1 = ConcolicMarker.mark(5, "int1");
	public static void test(String string1, int int1) {

		String string0 = "Togliere sta roba";

		int int0 = 5;
		char char0 = string0.charAt(int0);
		char char1 = string1.charAt(int1);

		checkEquals(char0, char1);

		// negative index throws exception
		try {
			string1.charAt(-1);
		} catch (StringIndexOutOfBoundsException ex) {
			// index too small
		}

		// too big index throws exception
		try {
			string1.charAt(Integer.MAX_VALUE);
		} catch (StringIndexOutOfBoundsException ex) {
			// index too small
		}
		
		// check everything still works here
		char char2 = string0.charAt(int1);
		char char3 = string1.charAt(int0);

		checkEquals(char2, char3);

	}
}
