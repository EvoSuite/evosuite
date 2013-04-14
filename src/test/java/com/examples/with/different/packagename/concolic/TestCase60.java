package com.examples.with.different.packagename.concolic;

import static org.evosuite.symbolic.Assertions.checkEquals;

public class TestCase60 {

	/**
	 * @param args
	 */
	// String string1 = ConcolicMarker.mark("Togliere sta roba", "string1");

	public static void test(String string1) {

		String string0 = "Togliere sta roba";

		int catchCount = 0;
		try {
			// begin index too small
			string1.substring(-1, 10);
		} catch (StringIndexOutOfBoundsException ex) {
			catchCount++;
		}

		try {
			// begin index too big
			string1.substring(Integer.MAX_VALUE, 10);
		} catch (StringIndexOutOfBoundsException ex) {
			catchCount++;
		}

		try {
			// end index too small
			string1.substring(0, -1);
		} catch (StringIndexOutOfBoundsException ex) {
			catchCount++;
		}

		try {
			// end index too big
			string1.substring(0, Integer.MAX_VALUE);
		} catch (StringIndexOutOfBoundsException ex) {
			catchCount++;
		}

		try {
			// end index less than begin index
			string1.substring(5, 2);
		} catch (StringIndexOutOfBoundsException ex) {
			catchCount++;
		}

		checkEquals(catchCount, 5); // 1==5! handler code is not symbolically
									// executed?

		String string2 = string0.substring(5, 12);
		String string3 = string1.substring(5, 12);

		checkEquals(string2.length(), string3.length());

	}
}
