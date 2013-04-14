package com.examples.with.different.packagename.concolic;

import static org.evosuite.symbolic.Assertions.checkEquals;

public class TestCase42 {

	private int[] intArray;

	/**
	 * @param args
	 */
	public static void test() {

		TestCase42 instance = new TestCase42();

		int[] myArray = new int[10];
		myArray[9] = Integer.MAX_VALUE;

		instance.intArray = myArray;
		instance.intArray[0] = Integer.MAX_VALUE;
		instance.intArray[1] = Integer.MIN_VALUE;
		instance.intArray[2] = (int) (Math.PI * 1000000);

		myArray[3] = (int) (Math.E * 1000000);

		int[] otherArray = new int[5];
		otherArray[3] = Integer.MAX_VALUE / 2;

		checkEquals(myArray[0], instance.intArray[0]);

	}

}
