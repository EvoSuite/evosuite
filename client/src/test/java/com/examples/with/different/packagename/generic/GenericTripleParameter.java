package com.examples.with.different.packagename.generic;

import java.util.HashMap;
import java.util.Map;

public class GenericTripleParameter<X, Y, Z> {
	
	public Map<X, Y> foo(Z param) {
		if(param == null)
			return new HashMap<X, Y>();
		else
			return new HashMap<X, Y>();
	}

}
