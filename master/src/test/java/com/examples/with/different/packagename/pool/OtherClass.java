package com.examples.with.different.packagename.pool;

public class OtherClass {

	public boolean testMe(DependencyClass other) {
		if(other.isFoo())
			return true;
		else
			return false;
	}
}
