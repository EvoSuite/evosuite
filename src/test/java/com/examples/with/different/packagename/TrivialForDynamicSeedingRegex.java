package com.examples.with.different.packagename;

public class TrivialForDynamicSeedingRegex {

	public static boolean foo(String x) {
		if (x.matches("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")) {
			return true;
		} else {
			return false;
		}
	}

}
