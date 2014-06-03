package com.examples.with.different.packagename;

public class FlagExample5 {

	public boolean testMe(int x, int y) {
		boolean flag = false;
		if(x == 34235) {
			if(x == y || y == -20362)
				flag = true;
		}
		return flag;
	}

	public void flagProblem(int x, int y) {
		if(testMe(x, y))
			System.out.println("Target");
	}
}
