package com.examples.with.different.packagename.staticusage;

public class DirectAccessStaticField {

	public static boolean foo() {
		if (SUTwithPublicStaticField.aPublicStaticField == 42) {
			return true;
		} else {
			return false;
		}
	}

}
