package com.examples.with.different.packagename;

public class FlagExample7 {

	boolean flag1 = false;

	private static boolean flagMe(int x) {
		return x == 762;
	}

	public void coverMe(int x) {
		if(flagMe(x)) {
			flag1 = true;
		}
	}

	public void coverMe2() {
		if(flag1) {
			System.out.println("Target");
		}
	  }
}
