package com.examples.with.different.packagename.pool;

public class DependencyClassWithException {

	private int x = 0;
	
	public void foo(int y) {
		if(y == 42)
			x++;
		else
			throw new RuntimeException("argh");
	}
	
	public boolean isFoo() {
		if(x == 5)
			return true;
		else
			return false;
	}
	
}
