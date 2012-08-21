package org.evosuite.symbolic;

import org.evosuite.symbolic.dsc.ConcolicMarker;
import static org.evosuite.symbolic.Assertions.checkEquals;

public class TestCase73 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String string0 = ConcolicMarker.mark("enci", "string0");
		String string1 = ConcolicMarker.mark("c", "string1");
		String result = "";
		result += string0.charAt(2);

		// StringBuilder sb = new StringBuilder(result);
		// sb.append(string0.charAt(2));
		// result = sb.toString();

		boolean boolean0 = result.equals(string1);
		checkEquals(boolean0, true);
		
		
	}

}
