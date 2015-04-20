package com.examples.with.different.packagename.concolic;

import java.util.regex.Pattern;

import static org.evosuite.symbolic.Assertions.checkEquals;

public class TestCase83 {

	/**
	 * @param args
	 */
	// String string0 = ConcolicMarker.mark("aaaaaab", "string0");
	// String string1 = ConcolicMarker.mark("bbbb", "string1");
	// int catchCount = ConcolicMarker.mark(0, "catchCount");
	public static void test(String string0, String string1, int catchCount) {
		String regex = "a*b";
		boolean boolean0 = Pattern.matches(regex, string0);
		checkEquals(boolean0, true);

		boolean boolean1 = Pattern.matches(regex, string1);
		checkEquals(boolean1, false);

		StringBuffer stringBuffer0 = new StringBuffer("aaaaaab");
		boolean boolean2 = Pattern.matches(regex, stringBuffer0);
		checkEquals(boolean2, true);

		try {
			boolean boolean3 = Pattern.matches(regex, null);
			checkEquals(boolean3, false);
		} catch (NullPointerException ex) {
			catchCount++;
		}

		try {
			boolean boolean3 = Pattern.matches(null, string0);
			checkEquals(boolean3, false);
		} catch (NullPointerException ex) {
			catchCount++;
		}

		try {
			boolean boolean3 = Pattern.matches(null, null);
			checkEquals(boolean3, false);
		} catch (NullPointerException ex) {
			catchCount++;
		}
		checkEquals(catchCount, 3);
	}
}
