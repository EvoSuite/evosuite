package com.examples.with.different.packagename.pool;

public class DependencyClass {

	protected int x = 0;

	public void foo(int y) {
		if (y == 42)
			x++;
	}

	public boolean isFoo() {
		if (x == 3)
			return true;
		else
			return false;
	}

}
