package com.examples.with.different.packagename;

public class GenericTwoDimensionalArray<T> {

	public boolean testMe(T[][] parameters, T obj) {
		if (parameters[0][0] == obj)
			return true;
		else
			return false;
	}
}
