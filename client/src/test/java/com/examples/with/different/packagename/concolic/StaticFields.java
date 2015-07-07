package com.examples.with.different.packagename.concolic;

public abstract class StaticFields {

	public static String string_field;

	public static Object object_field;

	public static boolean equals(String left, String right) {
		return left.equals(right);
	}
}