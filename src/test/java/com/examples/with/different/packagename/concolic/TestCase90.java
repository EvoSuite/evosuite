package com.examples.with.different.packagename.concolic;

import org.evosuite.symbolic.Assertions;

public class TestCase90 {

	public static void test(char char0) {
		int int0 = Character.getNumericValue(char0);
		int int1 = Character.getNumericValue('a');
		
		Assertions.checkEquals(int0, int1);
		
		boolean boolean0 = Character.isDigit(char0);
		boolean boolean1 = Character.isDigit('a');

		Assertions.checkEquals(boolean0, boolean1);

		boolean boolean2 = Character.isLetter(char0);
		boolean boolean3 = Character.isLetter('a');

		Assertions.checkEquals(boolean2, boolean3);

		
	}

}
