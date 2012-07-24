package org.evosuite.symbolic;

import org.evosuite.symbolic.dsc.ConcolicMarker;

import static org.evosuite.symbolic.Assertions.checkEquals;

public class TestCase28 {

	private static final String STRING_VALUE = "Togliere sta roba";

	public static void main(String[] args) {

		String string0 = ConcolicMarker.mark(STRING_VALUE, "string0");
		String string1 = STRING_VALUE;
		{
			boolean boolean0 = string0.equals(STRING_VALUE);
			boolean boolean1 = string1.equals(STRING_VALUE);
			checkEquals(boolean0, boolean1);
		}

		{
			boolean boolean0 = string0.equalsIgnoreCase(STRING_VALUE);
			boolean boolean1 = string1.equalsIgnoreCase(STRING_VALUE);
			checkEquals(boolean0, boolean1);
		}

		{
			boolean boolean0 = string0.endsWith(STRING_VALUE);
			boolean boolean1 = string1.endsWith(STRING_VALUE);
			checkEquals(boolean0, boolean1);
		}

		{
			boolean boolean0 = string0.startsWith(STRING_VALUE, 0);
			boolean boolean1 = string1.startsWith(STRING_VALUE, 0);
			checkEquals(boolean0, boolean1);
		}

		{
			boolean ignoresCase = true;
			int toffset = 10;
			String other = "STA";
			int ooffset = 0;
			int len = 9;
			boolean boolean0 = string0.regionMatches(ignoresCase, toffset,
					other, ooffset, len);
			boolean boolean1 = string1.regionMatches(ignoresCase, toffset,
					other, ooffset, len);
			checkEquals(boolean0, boolean1);
		}
	}
}
