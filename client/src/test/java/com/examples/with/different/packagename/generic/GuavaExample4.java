/**
 * 
 */
package com.examples.with.different.packagename.generic;

/**
 * @author Gordon Fraser
 * 
 */
public class GuavaExample4<T> {

	// The tricky bit here is that T refers to the type variable defined 
	// on the instance, and not the one on the return value (Iterable<S>)
	public <S extends T> GuavaExample4<Iterable<S>> create() {
		return new GuavaExample4<Iterable<S>>();
	}
}
