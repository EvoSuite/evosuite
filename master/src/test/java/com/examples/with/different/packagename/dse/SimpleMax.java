package com.examples.with.different.packagename.dse;

public class SimpleMax {

	private SimpleMax() {
		
	}
	
	public static int max(int x, int y) {
		new SimpleMax();
		if (x>y) {
			return x;
		} else {
			if (y==0) {
				return 0;
			} else {
				return x;
			}
		}
	}
}
