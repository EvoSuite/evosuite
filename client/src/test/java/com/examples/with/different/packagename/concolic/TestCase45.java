package com.examples.with.different.packagename.concolic;

import static org.evosuite.symbolic.Assertions.checkEquals;
import static org.evosuite.symbolic.Assertions.checkObjectEquals;

public class TestCase45 {

	/**
	 * @param args
	 */
	public static void test() {
		boolean[] boolArray = new boolean[12];
		boolean defaultValue = boolArray[0];
		checkEquals(false, defaultValue);

		TestCase45[] objectArray = new TestCase45[12];
		TestCase45 objectDefaultValue = objectArray[0];
		checkObjectEquals(null, objectDefaultValue);

		long[] longArray = new long[12];
		long longDefaultValue = longArray[0];
		checkEquals(0, longDefaultValue);
	}

}
