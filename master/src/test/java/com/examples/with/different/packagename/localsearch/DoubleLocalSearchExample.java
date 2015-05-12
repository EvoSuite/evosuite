package com.examples.with.different.packagename.localsearch;

public class DoubleLocalSearchExample {

	public boolean testMe(double x, double y) {
		if(Math.abs(x * Math.sqrt(Math.abs(y)) - 20 * Math.PI) < 0.01)
			return true;
		else
			return false;
	}
}
