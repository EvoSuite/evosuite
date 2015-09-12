package com.examples.with.different.packagename;

public class Euclidean {

	public int gcd(int a, int b) {
		if (b > a) {
			int t = a;
			a = b;
			b = t; 
		}

		while (b != 0) {
		    int m = a / b; // FIXME: should be '%'
		    a = b;
		    b = m;
		}

		return a;
	}
}
