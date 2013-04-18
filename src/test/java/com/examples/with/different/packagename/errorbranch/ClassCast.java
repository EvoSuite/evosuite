package com.examples.with.different.packagename.errorbranch;

public class ClassCast {

	@SuppressWarnings("unused")
	public void testMe(Object o) {
		String s = (String)o;
	}
}
