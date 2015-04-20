package com.examples.with.different.packagename.generic;

import java.util.List;

public class GenericArrayWithGenericType {

	public boolean testMe(List<?>[] parameters, List<?> obj) {
		if (parameters[1] == obj)
			return true;
		else
			return false;
	}
}
