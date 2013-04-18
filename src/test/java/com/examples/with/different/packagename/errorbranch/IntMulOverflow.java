package com.examples.with.different.packagename.errorbranch;

public class IntMulOverflow {

	protected final int LARGE_NUMBER = 2000000000;
	protected final int SMALL_NUMBER = -2000000000;

	public boolean testMe(int x, int y) {
		int z = x * y;
		if(z > 0)
			return true;
		else
			return false;
	}
}
