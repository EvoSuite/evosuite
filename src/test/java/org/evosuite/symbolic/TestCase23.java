package org.evosuite.symbolic;

import static org.evosuite.symbolic.Assertions.checkEquals;

import org.evosuite.symbolic.dsc.ConcolicMarker;

public class TestCase23 {

	public static final String STRING_VALUE = "Togliere sta roba";

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String string0 = ConcolicMarker.mark(STRING_VALUE, "string0");
		String string1 = STRING_VALUE;
		int int0 = string0.length();
		int int1 = string1.length();
		checkEquals(int0, int1);

	}

}
