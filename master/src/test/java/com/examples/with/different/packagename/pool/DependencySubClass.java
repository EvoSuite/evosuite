package com.examples.with.different.packagename.pool;

public class DependencySubClass extends DependencyClass {

	public boolean isFoo() {
		if(getX() == 4)
			return true;
		else
			return false;
	}

}
