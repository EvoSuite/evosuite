package com.examples.with.different.packagename.dse;

public class ObjectExample {

	public ObjectExample() {

	}

	public static int isNull(Object value, int x) {
		new ObjectExample();
		if (value == null) {
			if (x == 0) {
				return 1;
			} else {
				return 2;
			}
		} else {
			return 0;
		}
	}
}
