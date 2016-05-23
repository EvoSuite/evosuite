package com.examples.with.different.packagename.staticfield;

public class StaticBlockCoverage {

	private static boolean coverMe(int x) {
		if (x == 0) {
			return true;
		} else {
			return false;
		}
	}

	static {
		boolean val0 = coverMe(0);
		boolean val1 = coverMe(1);
	}

	public StaticBlockCoverage() {
		// do nothing
	}
}
