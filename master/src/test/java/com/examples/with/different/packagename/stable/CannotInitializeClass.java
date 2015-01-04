package com.examples.with.different.packagename.stable;

public class CannotInitializeClass {

	private static int value; 
	static {
		Object object = returnNull();
		if (object.equals(object)) {
			value = 1000;
		} else {
			value = 10;
		}
	}
	private static Object returnNull() {
		return null;
	}
	public static int init() {
		return value;
	}

}
