package com.examples.with.different.packagename.generic;

import java.util.List;

public class GenericArrayWithGenericTypeVariable<T> {

	public boolean testMe(List<T>[] parameters, List<T> obj) {
		if (parameters[1] == obj)
			return true;
		else
			return false;
	}
}
