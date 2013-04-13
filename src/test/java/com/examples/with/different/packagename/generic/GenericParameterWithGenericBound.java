package com.examples.with.different.packagename.generic;

import java.util.List;

public class GenericParameterWithGenericBound<S extends List<?>, T extends Number> {

	public boolean testMe(S list, T number) {
		if(list.contains(number))
			return true;
		else
			return false;
	}
	
}
