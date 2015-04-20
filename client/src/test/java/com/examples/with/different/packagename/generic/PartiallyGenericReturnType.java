package com.examples.with.different.packagename.generic;

import java.util.Collection;
import java.util.HashSet;

public class PartiallyGenericReturnType {

	@SuppressWarnings("rawtypes")
	public Collection<Class> foo(int x) {
		Collection<Class> bar = new HashSet<Class>();
		if(x == 42) {
			bar.add(getClass());
		}
		
		return bar;
	}
}
