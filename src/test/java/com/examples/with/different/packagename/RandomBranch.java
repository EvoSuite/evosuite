package com.examples.with.different.packagename;

import java.util.Random;

public class RandomBranch {

	public void testMe(int x) {
		Random r = new Random();
		int y = r.nextInt();
		if(y ==x && x + y > 0) {
			System.out.println("Target");
		}
	}
}
