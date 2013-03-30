package com.examples.with.different.packagename;

public class FlagExample6 {

	public void flagProblem(int x, int y) {
		FlagExample5 f = new FlagExample5();

		if(f.testMe(x, y))
			System.out.println("Target");
	}

}
