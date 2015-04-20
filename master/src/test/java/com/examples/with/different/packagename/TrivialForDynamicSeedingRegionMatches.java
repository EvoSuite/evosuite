package com.examples.with.different.packagename;

public class TrivialForDynamicSeedingRegionMatches {

	public static boolean foo(String x) {
		String a = "This string has to be long";
		String b = " Because in such case it would be hard for EvoSuite";
		if (x.regionMatches(6, a+b, 4, 10)) {
			return true;
		} else {
			return false;
		}
	}
}
