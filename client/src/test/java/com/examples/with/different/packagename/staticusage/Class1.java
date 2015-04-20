package com.examples.with.different.packagename.staticusage;

public class Class1 {

	public static boolean myMethod() {
		if (Class2.getIntField() == Integer.MAX_VALUE) {
			return true;
		} else {
			return false;
		}
	}
}
