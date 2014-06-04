package com.examples.with.different.packagename.pool;

public class ClassDependingOnExceptionClass {


	public boolean testMe(DependencyClassWithException other) {
		if(other.isFoo())
			return true;
		else
			return false;
	}
}
