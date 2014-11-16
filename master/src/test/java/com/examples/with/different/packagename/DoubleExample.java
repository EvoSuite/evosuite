package com.examples.with.different.packagename;

public class DoubleExample {

	public boolean testMe(double x, double y) {
		if(Math.abs(x * 2.4 - y + 100.0) < 2.0) {
			return true;
		} else {
			return false;
		}
	}
}
