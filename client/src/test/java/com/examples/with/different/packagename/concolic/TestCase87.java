package com.examples.with.different.packagename.concolic;

public class TestCase87 {

	public static boolean test(String string0) {
		if (string0.length() == 5 && string0.charAt(3) == '_'
				&& string0.charAt(4) == '+') {
			return false;
		}
		return true;
	}

}
