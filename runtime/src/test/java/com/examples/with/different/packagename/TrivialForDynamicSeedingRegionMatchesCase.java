package com.examples.with.different.packagename;

public class TrivialForDynamicSeedingRegionMatchesCase {

	public static boolean foo2(String x) {
		String a = "In this test case we are using another string";
		String b = " that is also very hard for EvoSuite";
		if (x.regionMatches(true, 6, a+b, 4, 10)) {
			return true;
		} else {
			return false;
		}
	}
}
