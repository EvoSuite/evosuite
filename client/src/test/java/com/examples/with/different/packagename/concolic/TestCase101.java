package com.examples.with.different.packagename.concolic;

public class TestCase101 {

	public static boolean test(Class<?> clazz, String className) {
		if (className.equals(clazz.getName())) {
			return true;
		} else {
			return false;
		}

	}

}
