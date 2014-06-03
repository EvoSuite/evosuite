package com.examples.with.different.packagename.generic;

public class GenericSUTTwoParameters<T, T2> {
	public boolean testMe(T t1, T2 t2) {
		if(t1.equals(t2)) {
			return true;
		} else {
			return false;
		}
	}
}
