package com.examples.with.different.packagename.staticfield;

public class SaticSingletonField {

	private int value;

	private static SaticSingletonField instance = new SaticSingletonField();

	public static SaticSingletonField getInstance() {
		return instance;
	}

	public int getValue() {
		return value;
	}

	public void incValue() {
		value++;
	}

	public static int checkMe() {
		if (getInstance().getValue() == 0) {
			return 0;
		} else if (getInstance().getValue() == 1) {
			return 1;
		} else if (getInstance().getValue() == 2) {
			return 2;
		} else {
			return Integer.MAX_VALUE;
		}
	}

}
