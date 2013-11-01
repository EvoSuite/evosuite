package com.examples.with.different.packagename.localsearch;

public class StringLocalSearchExample {

	public boolean testMe(String x, String y) {
		String xReversed = new StringBuilder(x).reverse().toString();
		if(x.length() > 4 && xReversed.equals(y))
			return true;
		else
			return false;
	}
}
