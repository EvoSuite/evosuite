package com.examples.with.different.packagename;

public class ArrayLimit {
	
	public void foo(int x){
		
		int[] largeArray = new int[1000];
	
		if(x>0){
			largeArray[0]++;
		} else {
			largeArray[0]--;
		}
	}

}
