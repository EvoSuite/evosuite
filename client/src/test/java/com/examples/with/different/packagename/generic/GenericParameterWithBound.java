package com.examples.with.different.packagename.generic;

import java.util.List;

public class GenericParameterWithBound<T extends List<?>> {

	public boolean testMe(T t, Integer x) {
		if (t instanceof List) {
			if (((List<?>) t).size() == 3)
				return true;
		}
		return false;
	}
}
