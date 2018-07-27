package com.examples.with.different.packagename.dse;

public class Max {

	public Max() {
		// branchless method
	}

	public static int max(int x, int y, int z) {
		new Max();
		int max0;
		if (x > y) {
			max0 = x;
		} else {
			max0 = y;
		}
		int max1;
		if (y > z) {
			max1 = y;
		} else {
			max1 = z;
		}
		if (max0 > max1) {
			return max0;
		} else {
			return max1;
		}
	}

}