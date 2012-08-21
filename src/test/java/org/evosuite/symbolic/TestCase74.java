package org.evosuite.symbolic;

import org.evosuite.symbolic.dsc.ConcolicMarker;
import static org.evosuite.symbolic.Assertions.checkEquals;

public class TestCase74 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String string0 = ConcolicMarker.mark("Togliere ", "string0");
		String string1 = ConcolicMarker.mark("sta ", "string1");
		String string2 = ConcolicMarker.mark("roba ", "string2");
		String string3 = "Togliere sta roba ";

		StringBuilder sb = new StringBuilder(string0);

		String string4 = sb.toString();
		boolean boolean0 = string4.equals(string3);
		checkEquals(boolean0, false);

		String string5 = sb.append(string1).toString();
		boolean boolean1 = string5.equals(string3);
		checkEquals(boolean1, false);

		String string6 = sb.append(string2).toString();
		boolean boolean2 = string6.equals(string3);
		checkEquals(boolean2, true);
		
		
		String string7 = sb.append((String)null).toString();
		String string8 = "Togliere sta roba null";
		boolean boolean3 = string7.equals(string8);
		checkEquals(boolean3, true);
	}

}
