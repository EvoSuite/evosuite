package com.examples.with.different.packagename.errorbranch;

public class IntDivOverflow {

	protected final int SMALL_VALUE = Integer.MIN_VALUE;
	protected final int MINUS_ONE = -1;
			
	public boolean testMe(int x, int y) {
		int z = x + y;
		if(z > 0)
			return true;
		else
			return false;
	}
	
}
