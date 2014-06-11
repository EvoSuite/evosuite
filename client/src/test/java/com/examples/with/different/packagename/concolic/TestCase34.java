package com.examples.with.different.packagename.concolic;


import static org.evosuite.symbolic.Assertions.checkEquals;

public class TestCase34 {

	private Object stored_value;
	
	/**
	 * @param args
	 */
	public static void test(String string0) {
		String string1 = string0.toUpperCase();
		TestCase34 testCase34 = new TestCase34();
		testCase34.stored_value = string1;
		Object stored_value = testCase34.stored_value;
		String string2 = (String)stored_value;
		String string3 = "Togliere sta roba".toUpperCase();
		int int0 = string2.length();
		int int1 = string3.length();
		checkEquals(int0,int1);
	}

}
