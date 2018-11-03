package com.examples.with.different.packagename.dse;

public class StringExample {

	public StringExample() {
		
	}

	public static int foo(String str) {
		new StringExample();
		if (str == null) {
			return 1;
		} else {
			if (str.length() == 1)
				return 2;
			else
				return 3;
		}
	}
}
