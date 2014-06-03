package com.examples.with.different.packagename;

public class FlagExample2 {

	public void testMe(int x) {
		boolean flag = x == 23482 || x == 1235;
		if(flag) {
			System.out.println("Target");
		}
	}
}
