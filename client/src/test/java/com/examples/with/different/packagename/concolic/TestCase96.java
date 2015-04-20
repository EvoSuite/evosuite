package com.examples.with.different.packagename.concolic;

import org.evosuite.symbolic.Assertions;

public class TestCase96 {

	public static void test(String string0, int int0) {
		StringBuffer buf = new StringBuffer();
		buf.append(string0);
		buf.setLength(5);
		String string1 = buf.toString();
		Assertions.checkEquals(5, string1.length());
		buf.setLength(0);
		String string2 = buf.toString();
		Assertions.checkEquals(int0, string2.length());
	}
}
