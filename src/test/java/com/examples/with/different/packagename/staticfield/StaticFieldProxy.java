package com.examples.with.different.packagename.staticfield;

public abstract class StaticFieldProxy {

	public static int inc() throws IllegalStateException {
		return StaticField.inc();
	}

	public static int reset() throws IllegalStateException {
		return StaticField.reset();
	}

	public static boolean checkCounter() {
		return StaticField.checkCounter();
	}

	public static int getCounter() {
		return StaticField.getCounter();
	}

	public static boolean myCounter() {
		if (StaticField.getCounter()==0)
			return true;
		else
			return false; // unreachable
	}
}
