package com.examples.with.different.packagename;

public class FlagExample3 {

	private boolean isFive(int x) {
		return x == 5;
	}

	public boolean getFlag(int x) {
		if(isFive(x))
			return true;
		else
			return false;
	}
}
