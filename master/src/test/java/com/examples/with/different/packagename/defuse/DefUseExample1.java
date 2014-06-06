package com.examples.with.different.packagename.defuse;

public class DefUseExample1 {

	public int testMe(int x) {
		int y = 0;
		
		if(x == 27)
			y = y + 3;
		
		return y;
	}
}
