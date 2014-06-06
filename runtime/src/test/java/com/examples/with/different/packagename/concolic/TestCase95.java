package com.examples.with.different.packagename.concolic;

import java.util.StringTokenizer;

import org.evosuite.symbolic.Assertions;

public class TestCase95 {

	public static void test(String string, String delim) {
		StringTokenizer tokenizer = new StringTokenizer(string,delim);

		int i=0;
		while (tokenizer.hasMoreTokens()) {
			String nextToken = tokenizer.nextToken();
			int length = nextToken.length();
			if (i==0) {
				Assertions.checkEquals("Togliere".length(), length);
			} else if (i==1) {
				Assertions.checkEquals("sta".length(), length);
			} else if (i==2) {
				Assertions.checkEquals("roba".length(), length);
			}
			i++;
		}
	}
}
