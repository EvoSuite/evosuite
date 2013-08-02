/**
 * 
 */
package com.examples.with.different.packagename.generic;

import java.util.Locale;

/**
 * @author Gordon Fraser
 * 
 */
public abstract class AbstractGenericClass<T> {

	private int value = 0;

	public AbstractGenericClass(int value) {
		this.value = value;
	}

	boolean testMe(AbstractGenericClass<Locale> other) {
		if (value == other.value)
			return true;
		else
			return false;
	}

}
