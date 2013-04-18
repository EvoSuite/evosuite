package com.examples.with.different.packagename.errorbranch;

public class ArrayAccess {

	@SuppressWarnings("unused")
	public void testMe(int x) {
		int[] test = new int[10];
		int y = test[x];
	}
}
