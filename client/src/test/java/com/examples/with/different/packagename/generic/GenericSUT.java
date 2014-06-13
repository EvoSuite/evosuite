package com.examples.with.different.packagename.generic;

public class GenericSUT<T> {

	public boolean testMe(T t1, T t2) {
		if(t1.equals(t2)) {
			return true;
		} else {
			return false;
		}
	}
	
}
