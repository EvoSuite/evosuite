package com.examples.with.different.packagename.solver;

import java.util.StringTokenizer;

public class TestCaseTokenizer {

	public static boolean test(String str) {
		StringTokenizer tokenizer = new StringTokenizer(str," ");
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if (token.equals("Ramon")) {
				return true;
			}
		}
		return false;
	}

}
