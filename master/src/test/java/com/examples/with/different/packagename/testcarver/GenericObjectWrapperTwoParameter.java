package com.examples.with.different.packagename.testcarver;

public class GenericObjectWrapperTwoParameter<S, T extends Comparable<T>> {

	private T value = null;
	
	public T getValue() {
		return value;
	}
	
	public void setValue(T val) {
		value = val;
	}
	
	public boolean isEqual(S other) {
		return other.equals(value);
	}
	
}
