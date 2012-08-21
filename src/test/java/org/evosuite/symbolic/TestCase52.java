package org.evosuite.symbolic;

import org.evosuite.symbolic.dsc.ConcolicMarker;

public class TestCase52 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String string0 = ConcolicMarker.mark("Togliere sta roba", "string0");
		String string1 = ConcolicMarker.mark(" ", "string1");
		String[] stringArray0 = string0.split(string1);

		if (stringArray0.length != 3) {
			throw new RuntimeException();
		}
	}

}
