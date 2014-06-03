package com.examples.with.different.packagename;

public class FlagExample4 {

	public void flag6(int x, int y) {
		boolean flag1 = x == 2904;
		boolean flag2 = false;
		if(flag1) {
			if(y == 23598)
				flag2 = true;
		}
		else {
			if(y == 223558)
				flag2 = true;
		}
		if(flag2)
			System.out.println("Target");
	}
}
