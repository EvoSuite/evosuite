package com.examples.with.different.packagename.dse;

public abstract class Min {

	public Min() {
	}

	/**
	 * Entry point method with 2 branches (x>y and x<=y)
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public static int min(int x, int y) {
		if (x > y) {
			return y;
		} else {
			return x;
		}
	}
}
