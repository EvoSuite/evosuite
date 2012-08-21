package org.evosuite.symbolic;

import org.evosuite.symbolic.dsc.ConcolicMarker;
import static org.evosuite.symbolic.Assertions.checkEquals;

public class TestCase64 {

	public static void main(String[] args) {
		String string0 = "Togliere sta roba";
		String string1 = ConcolicMarker.mark(string0, "string0");

		int catchCount = 0;

		try {
			string1.concat(null);
		} catch (NullPointerException ex) {
			catchCount++;
		}

		checkEquals(1, catchCount);

		String string2 = string1.concat(string0);
		String string3 = "Togliere sta roba" + "Togliere sta roba";
		boolean boolean0 = string2.equals(string3);
		
		checkEquals(true,boolean0);
	}
}
