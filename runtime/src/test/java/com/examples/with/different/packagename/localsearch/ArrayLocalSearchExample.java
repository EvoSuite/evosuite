package com.examples.with.different.packagename.localsearch;

public class ArrayLocalSearchExample {

	public boolean testMe(int[] x) {
		if(x.length == 4) {
			if(x[0] != 0)
				return false;
			if(x[1] != 10)
				return false;
			if(x[2] != 20)
				return false;
			if(x[3] != 30)
				return false;
			
			return true;
		}
		
		return false;
	}
}
