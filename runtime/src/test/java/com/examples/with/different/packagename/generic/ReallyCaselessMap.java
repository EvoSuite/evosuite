package com.examples.with.different.packagename.generic;

import java.util.HashMap;

public class ReallyCaselessMap<V> extends HashMap<String, V> {

	private static final long serialVersionUID = -4166367115932977434L;

	public boolean testMe(String key, V object) {
		if(get(key) == object)
			return true;
		else
			return false;
	}
}
