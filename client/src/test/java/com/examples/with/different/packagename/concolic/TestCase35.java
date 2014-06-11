package com.examples.with.different.packagename.concolic;


import static org.evosuite.symbolic.Assertions.checkEquals;

public class TestCase35 {

	public static final String STRING_VALUE = "Togliere sta roba";

	private Object objectField;

	public static void test(String string0) {

		String string1 = STRING_VALUE;
		TestCase35 testCase35 = new TestCase35();
		testCase35.objectField = "ere";
		CharSequence charSequence0 = (CharSequence) testCase35.objectField;
		Object object1 = "q";
		CharSequence charSequence1 = (CharSequence) object1;

		{
			boolean boolean0 = string0.equals(testCase35.objectField);
			boolean boolean1 = string1.equals(testCase35.objectField);
			checkEquals(boolean0, boolean1);
		}

		{
			boolean boolean0 = string0.contains(charSequence0);
			boolean boolean1 = string1.contains(charSequence0);
			checkEquals(boolean0, boolean1);
		}

		{
			String string2 = string0.replace(charSequence0, charSequence1);
			String string3 = string1.replace(charSequence0, charSequence1);
			int int0 = string2.length();
			int int1 = string3.length();
			checkEquals(int0, int1);
		}
	}
}
