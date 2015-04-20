package com.examples.with.different.packagename;

public class IntExample {

	public boolean testMe(int x, int y) {
		if(x * y == 0)
			return false;
		
		if(x == y * 22) {
			return true;
		} else {
			return false;
		}
	}
}
