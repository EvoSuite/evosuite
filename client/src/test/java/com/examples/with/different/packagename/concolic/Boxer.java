package com.examples.with.different.packagename.concolic;

public abstract class Boxer {

	public static Integer boxInteger(Integer i) {
		return i;
	}

	public static int unboxInteger(int i) {
		return i;
	}
}