package com.examples.with.different.packagename.concolic;

public class TestCase100 {

	public static boolean test(String string0, int int0) {
		int intValue = int0;
		try {
			Integer i = Integer.parseInt(string0);
			intValue = i.intValue();
		} catch (NumberFormatException ex) {
			if (intValue == -1) {
				return true;
			}
		}
		return false;
	}

}
