package org.evosuite.symbolic;

import static org.evosuite.symbolic.Assertions.checkEquals;

import org.evosuite.symbolic.dsc.ConcolicMarker;

public class TestCase85 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String regex = "a*b";
		String string0 = ConcolicMarker.mark("aaaaaaaaaaab", "string0");

		boolean boolean0 = string0.matches(regex);

		checkEquals(boolean0, true);
	}

}
