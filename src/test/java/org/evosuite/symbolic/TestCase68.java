package org.evosuite.symbolic;

import static org.evosuite.symbolic.Assertions.checkEquals;

import org.evosuite.symbolic.dsc.ConcolicMarker;

public class TestCase68 {

	public static void main(String[] args) {
		String string0 = "Togliere sta roba";
		String string1 = ConcolicMarker.mark(string0, "string0");

		int catchCount = 0;

		try {
			string1.contains(null);
		} catch (NullPointerException ex) {
			catchCount++;
		}
		checkEquals(1, catchCount);

		boolean boolean0 = string1.contains(new StringBuffer().toString());

		checkEquals(true, boolean0);

		boolean boolean1 = string1.contains("sta");
		boolean boolean2 = string0.contains("sta");

		checkEquals(boolean1, boolean2);

		boolean boolean3 = string1.contains(string1);
		boolean boolean4 = string0.contains(string1);

		checkEquals(boolean4, boolean3);

	}
}
