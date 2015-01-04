package com.examples.with.different.packagename.reset;

public class StaticInitThrowsNullPointer {

	static {
		someMethod();
	}

	private static void someMethod() {
		throw new NullPointerException("A null pointer exception!");
	}

}
