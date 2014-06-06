package com.examples.with.different.packagename.stable;

public class CanInitializeClass {

	private static int counter;
	static {
		counter=100500+10;
	}
	private final int value;
	public CanInitializeClass() {
		value = counter;
	}
	public boolean isValue(int value) {
		if (value==this.value) {
			return true;
		} else {
			return false;
		}
	}
	public static int useNotInitializedClass() {
		int value = CannotInitializeClass.init();
		return value;
	}
}
