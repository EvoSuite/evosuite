/**
 * 
 */
package com.examples.with.different.packagename.generic;

/**
 * @author Gordon Fraser
 * 
 */
public class GenericArray<T> {

	public boolean testMe(T[] parameters, T obj) {
		if (parameters[1] == obj)
			return true;
		else
			return false;
	}
}
