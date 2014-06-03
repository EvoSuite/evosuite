package com.examples.with.different.packagename.concolic;

import static org.evosuite.symbolic.Assertions.checkEquals;

public class TestCase63 {

//	String string0 = ConcolicMarker.mark(string1, "string0");
	public static void test(String string0) {
		String string1 = "Togliere sta roba";

		int catchCount = 0;

		try {
			string0.compareTo(null);
		} catch (NullPointerException ex) {
			catchCount++;
		}

		try {
			string0.compareToIgnoreCase(null);
		} catch (NullPointerException ex) {
			catchCount++;
		}

		try {
			string0.endsWith(null);
		} catch (NullPointerException ex) {
			catchCount++;
		}

		checkEquals(3, catchCount);

		int int0 = string0.compareTo(string1.toLowerCase());
		int int1 = string0.compareToIgnoreCase(string1.toLowerCase());
		boolean boolean0 = string0.endsWith("roba");
		boolean boolean1 = string0.equalsIgnoreCase(string1.toUpperCase());
		int int2 = string1.compareTo(string1.toLowerCase());
		boolean boolean2 = string0.equalsIgnoreCase(null);
		boolean boolean3 = string0.equals(null);

		boolean boolean5 = string0.equals("Togliere sta roba");

		checkEquals(int0, int2);
		checkEquals(int1, 0);
		checkEquals(boolean0, true);
		checkEquals(boolean1, true);
		checkEquals(boolean2, false);
		checkEquals(boolean3, false);
		checkEquals(boolean5, true);

//		Object object0 = new Object();
//		boolean boolean4 = string0.equals(object0);
//		checkEquals(boolean4, false);
	}
}
