package com.examples.with.different.packagename.concolic;

import static org.evosuite.symbolic.Assertions.checkEquals;


public class TestCase27 {

	public static final String STRING_VALUE = "Togliere sta roba";

	private static final char OLD_CHAR = 's';

	private static final char NEW_CHAR = 't';

	private static final String OLD_STRING = "sta";

	private static final String NEW_STRING = "xay";

	/**
	 * @param args
	 */
	public static void test(String string0) {
		String string1 = STRING_VALUE;
		{
			// branch 1
			String string2 = string0.replace(OLD_CHAR, NEW_CHAR);
			String string3 = string1.replace(OLD_CHAR, NEW_CHAR);
			int int0 = string2.length();
			int int1 = string3.length();
			checkEquals(int0, int1);
		}

		{
			// branch 2
			String string2 = string0.replaceAll(OLD_STRING, NEW_STRING);
			String string3 = string1.replaceAll(OLD_STRING, NEW_STRING);
			int int0 = string2.length();
			int int1 = string3.length();
			checkEquals(int0, int1);
		}

		{
			// branch 3
			String string2 = string0.replaceFirst(OLD_STRING, NEW_STRING);
			String string3 = string1.replaceFirst(OLD_STRING, NEW_STRING);
			int int0 = string2.length();
			int int1 = string3.length();
			checkEquals(int0, int1);
		}

		{
			// branch 4
			int int0 = string0.indexOf(OLD_CHAR, 0);
			int int1 = string1.indexOf(OLD_CHAR, 0);
			checkEquals(int0, int1);
		}

		{
			// branch 5
			int int0 = string0.lastIndexOf(OLD_CHAR, 0);
			int int1 = string1.lastIndexOf(OLD_CHAR, 0);
			checkEquals(int0, int1);
		}

		{
			// branch 6
			int int0 = string0.indexOf(OLD_STRING, 0);
			int int1 = string1.indexOf(OLD_STRING, 0);
			checkEquals(int0, int1);
		}

		{
			// branch 7
			int int0 = string0.lastIndexOf(OLD_STRING, 0);
			int int1 = string1.lastIndexOf(OLD_STRING, 0);
			checkEquals(int0, int1);
		}

		{
			// branch 8
			String string2 = string0.substring(2, 10);
			String string3 = string1.substring(2, 10);
			int int0 = string2.length();
			int int1 = string3.length();
			checkEquals(int0, int1);
		}
	}

}
