package com.examples.with.different.packagename.concolic;

public class TestCase33 {

	public static void test(String string0) {
		bar(string0);
	}

	public static boolean bar(String s) {
		StringBuffer bf = new StringBuffer();
		bf.append('b');
		bf.append('a');
		bf.append('r');
		String bf_str = bf.toString();
		if (s.equals(bf_str)) {
			return true;
		} else {
			return false;
		}
	}
}
