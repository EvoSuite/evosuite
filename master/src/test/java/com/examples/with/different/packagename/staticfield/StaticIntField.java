package com.examples.with.different.packagename.staticfield;

public class StaticIntField {

	private static int value = 0;

	public static void incValue() {
		value++;
	}

	public static int getValue() {
		return value;
	}

	public int checkMe() {
		if (getValue() == 0) {
			return 0;
		} else if (getValue() == 1) {
			return 1;
		} else if (getValue() == 2) {
			return 2;
		} else {
			return Integer.MAX_VALUE;
		}
	}
}
