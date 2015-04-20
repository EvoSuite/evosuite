package com.examples.with.different.packagename.staticfield;

public class StaticFoo {

	public StaticFoo() {}
	
	private static int counter = 0;

	public static boolean bar(int value) throws IllegalStateException {
		if (counter > 0)
			throw new IllegalStateException("Only one call to bar is allowed");

		counter++;
		if (value < 0)
			return true;
		else
			return false;
	}
	
	public static int getCounter() {
		return counter;
	}
}
