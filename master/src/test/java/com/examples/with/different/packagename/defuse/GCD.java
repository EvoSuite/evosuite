package com.examples.with.different.packagename.defuse;

public class GCD {

	public int gcd(int x, int y) {
		int tmp;
		
		while(y != 0) {
			tmp = x % y;
			x = y;
			y = tmp;
		}
		
		return x;
	}
}
