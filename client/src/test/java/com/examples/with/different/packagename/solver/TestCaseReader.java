package com.examples.with.different.packagename.solver;

import java.io.IOException;
import java.io.StringReader;

public class TestCaseReader {

	public static boolean test(String str) {
		try {
			StringReader reader = new StringReader(str);
			while (reader.read() != -1) {
				// skip
			}
			return false;
		} catch (IOException e) {
			return false;
		}
	}

}
