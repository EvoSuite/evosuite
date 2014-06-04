package com.examples.with.different.packagename.localsearch;

public class DoubleLocalSearchExample {

	public boolean testMe(double x, double y) {
		if(Math.abs((Math.log(x) * Math.sqrt(y)) - 20 * Math.PI) < 0.01)
			return true;
		else
			return false;
	}
}
