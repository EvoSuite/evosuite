package com.examples.with.different.packagename.coverage;

public class IntExampleWithNoElse {

	public boolean testMe(int x, int y) {
		if(x * y == 0)
			return false;
		
		if(x == y * 22) {
			System.out.println("Test");
		}
		
		return false;
	}
}
