package com.examples.with.different.packagename.generic;

import java.util.HashMap;
import java.util.Map;

public class GuavaExample<R, C, V> {
	
	private R value;
	
	private GuavaExample(R value) {
		this.value = value;
	}
	
	public static <R extends Comparable, C extends Comparable, V> GuavaExample<R, C, V> create(R value) {
		return new GuavaExample<R, C, V>(value);
	}
	
	public Map<C, V> row(R rowKey) {
	    Map<C,V> map = new HashMap<C, V>();
	    if(rowKey == value)
	    	return map;
	    else
	    	return map;
	}
}
