package com.examples.with.different.packagename.dse;

public abstract class MinUnreachableCode {

	public MinUnreachableCode() {
		// branchless constructor
	}
	
	public static int min(int x, int y) {
		if (x<y) {
			return x;
		} else {
			if (x<y) {
				// unreachable
				return x;
			} else {
				return y;
			}
		}
	}
	
}
