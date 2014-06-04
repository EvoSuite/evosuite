package com.examples.with.different.packagename;

public class TrivialForDynamicSeedingRegex {

	public static final String REGEX = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	
	public static boolean foo(String x) {
		if (x.matches(REGEX)) {
			return true;
		} else {
			return false;
		}
	}

}
