/**
 * 
 */
package com.examples.with.different.packagename.testcarver;

/**
 * @author Gordon Fraser
 * 
 */
public class GenericObjectWrapper<T> {

	private T obj;

	public void set(T obj) {
		this.obj = obj;
	}

	public T get() {
		return obj;
	}
}
