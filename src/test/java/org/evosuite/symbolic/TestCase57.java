package org.evosuite.symbolic;

import org.evosuite.symbolic.dsc.ConcolicMarker;
import static org.evosuite.symbolic.Assertions.checkEquals;

public class TestCase57 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String string0 = "Togliere sta roba";
		String string1 = ConcolicMarker.mark("Togliere sta roba".toUpperCase(),
				"string1");

		int int0 = string0.length();
		int int1 = string1.length();

		checkEquals(int0, int1);

		try {
			String string2 = null;
			int int2 = string2.length();
		} catch (NullPointerException ex) {
			System.out.println("Hello world!");
		}

		String string3 = ConcolicMarker.mark("Togliere", "string3");
		int int3 = string3.length();

		checkEquals(int1, int3);
	}
}
