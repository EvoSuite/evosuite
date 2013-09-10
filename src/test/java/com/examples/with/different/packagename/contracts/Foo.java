package com.examples.with.different.packagename.contracts;

public class Foo {

	private int x = 0;
	
	public void foo() {
		if(x == 3)
			x = -2;		
		x++;
	}
	
	public int getX() {
		return x;
	}
	
}
