package com.examples.with.different.packagename.generic;

public class EqualPredicate<T> implements Predicate<T> {

	private T value;
	
    public EqualPredicate(final T object) {
        this.value = object;
    }
    
    public boolean evaluate(final T object) {
    	if(value == null)
    		return object == null;
    	else
    		return value.equals(object);
    }
}
