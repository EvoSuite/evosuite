package org.evosuite.symbolic;

import org.evosuite.symbolic.dsc.ConcolicMarker;
import static org.evosuite.symbolic.Assertions.checkEquals;

public class TestCase58 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String string0 = "Togliere sta roba";
		String string1 = ConcolicMarker.mark("Togliere sta roba".toUpperCase(),
				"string1");

		int int0 = string0.toUpperCase().length();
		int int1 = string1.toUpperCase().length();

		checkEquals(int0, int1);

		String string2 = string0.toLowerCase().trim();
		String string3 = string1.toLowerCase().trim();

		int int2 = string2.toUpperCase().length();
		int int3 = string3.toUpperCase().length();

		checkEquals(int2, int3);
	}
}
