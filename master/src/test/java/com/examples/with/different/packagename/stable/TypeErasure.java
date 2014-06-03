package com.examples.with.different.packagename.stable;

public class TypeErasure<E> {

	private final E value;
	private final Object[] arrayOfE;
	
	public TypeErasure(E value) throws IllegalArgumentException{
		if (value==null)
			throw new IllegalArgumentException();
		
		if (value instanceof Object[])
			throw new IllegalArgumentException();
		
		this.value = value;
		this.arrayOfE = new Object[1];
	}
	
	@SuppressWarnings("unchecked")
	public E executionCausesClassCastException() {
		return (E) arrayOfE;
	}
	
	public E executionIsFine() {
		return value;
	}
	
}
