package com.examples.with.different.packagename.generic;

import java.util.ArrayList;
import java.util.List;

public class GenericClassTwoParameters<K, V> {

	private GenericClassTwoParameters() {
		
	}
	
	public static <X, Y> GenericClassTwoParameters<X, Y> create() {
		return new GenericClassTwoParameters<X, Y>();
	}
	
	public List<V> get(K key) {
		return new ArrayList<V>();
	}
	
	public int testMe() {
		return 0;
	}
	
}
