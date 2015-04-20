package com.examples.with.different.packagename.concolic;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.evosuite.symbolic.Assertions.checkEquals;

public class TestCase84 {

	/**
	 * @param args
	 */
	// String string0 = ConcolicMarker.mark("aaaaaaaaaaab", "string0");
	public static void test(String string0) {

		String regex = "a*b";

		Pattern pattern0 = Pattern.compile(regex);
		Matcher matcher0 = pattern0.matcher(string0);
		boolean boolean0 = matcher0.matches();

		checkEquals(boolean0, true);
	}

}
