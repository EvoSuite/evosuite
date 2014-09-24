package com.examples.with.different.packagename.concolic;

public class TestCase100 {

	public static boolean test(String myStr) {
		int intValue = -1;
		try {
			Integer i = Integer.parseInt(myStr);
			intValue = i.intValue();
		} catch (NumberFormatException ex) {
			if (intValue == -1) {
				return true;
			}
		}
		return false;
	}

}
