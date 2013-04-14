package com.examples.with.different.packagename.concolic;

import static org.evosuite.symbolic.Assertions.checkEquals;

public class TestCase39 {

	private static final String TOGLIERE = "Togliere";
	private static final String STA = "sta";
	public static final String ROBA = "roba";

	/**
	 * @param args
	 */
	public static void test(int int0, String string2) {
		String[] stringArray0 = new String[int0];
		String string0 = TOGLIERE;
		String string1 = STA;
		stringArray0[5] = string0;
		stringArray0[6] = string1;
		stringArray0[7] = string2;

		boolean boolean0 = stringArray0[7].equalsIgnoreCase(ROBA.toUpperCase());
		boolean boolean1 = ROBA.equalsIgnoreCase(ROBA.toUpperCase());

		checkEquals(boolean0, boolean1);

		stringArray0[5] = string2.concat(string2);

		boolean boolean2 = stringArray0[5].equals("robaroba");
		boolean boolean3 = (ROBA.concat(ROBA)).equals("robaroba");

		checkEquals(boolean2, boolean3);

	}
}
