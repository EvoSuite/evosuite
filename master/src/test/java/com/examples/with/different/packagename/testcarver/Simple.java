package com.examples.with.different.packagename.testcarver;

public class Simple {

	private int x;
	private final int y = 3;
	
	public boolean incr(){
		x++;
		return x == y;
	}
	
	public boolean sameValues(int z, int w){
		return z==w;
	}
}
