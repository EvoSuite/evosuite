package com.examples.with.different.packagename.generic;

import java.util.LinkedList;
import java.util.List;

public class GenericWithWildcardParameter<T> {

	
	public boolean foo(List<? extends T> x) {
		if(x instanceof LinkedList)
			return true;
		else
			return false;
	}
}
