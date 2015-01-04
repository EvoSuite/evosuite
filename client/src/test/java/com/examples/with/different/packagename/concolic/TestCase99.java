package com.examples.with.different.packagename.concolic;

public class TestCase99 {

	public static boolean test(String myStr) {
		Integer i = Integer.parseInt(myStr);
		int intValue = i.intValue();
		if (intValue == 10) {
			return true;
		} else {
			return false;
		}
	}

}
