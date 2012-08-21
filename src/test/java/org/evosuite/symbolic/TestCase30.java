package org.evosuite.symbolic;

import org.evosuite.symbolic.dsc.ConcolicMarker;

import static org.evosuite.symbolic.Assertions.checkEquals;

public class TestCase30 {

	private static final String STRING_VALUE = "Togliere sta roba";

	private static final Object SOME_OBJECT = new Object();

	public static void main(String[] args) {

		String string0 = ConcolicMarker.mark(STRING_VALUE, "string0");
		String string1 = STRING_VALUE;
		{
			boolean boolean0 = string0.equals(SOME_OBJECT);
			boolean boolean1 = string1.equals(SOME_OBJECT);
			checkEquals(boolean0, boolean1);
		}
	}
}
