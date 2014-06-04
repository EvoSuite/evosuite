package com.examples.with.different.packagename.concolic;

import org.evosuite.symbolic.Assertions;
import org.junit.Test;

public class TestCase86 {

	@Test
	public void runTest() {
		int int0 = Integer.MAX_VALUE;
		test(int0);
	}

	public static void test(int int0) {
		int[] intArray0 = new int[10];
		intArray0[0] = int0;
		int[] intArray1 = intArray0.clone();
		int int1 = intArray1[0];
		Assertions.checkEquals(int0, int1);
		int int2 = intArray0.length;
		int int3 = intArray1.length;
		Assertions.checkEquals(int2, int3);

		TestCase86[] objectArray0 = new TestCase86[10];
		TestCase86 instance = new TestCase86();
		objectArray0[0] = instance;
		TestCase86[] objectArray1 = objectArray0.clone();
		int int4 = objectArray0.length;
		int int5 = objectArray1.length;
		Assertions.checkEquals(int4, int5);
	}
}
