package com.examples.with.different.packagename.dse;

public class BooleanExample {

	public BooleanExample() {

	}

	public static int isTrue(boolean value) {
		new BooleanExample();
		if (value == true) {
			return 1;
		} else {
			return 0;
		}
	}
}
