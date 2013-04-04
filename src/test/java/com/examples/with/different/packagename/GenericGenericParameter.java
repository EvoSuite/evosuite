/**
 * 
 */
package com.examples.with.different.packagename;

import java.util.List;

/**
 * @author Gordon Fraser
 * 
 */
public class GenericGenericParameter<T> {

	public boolean testMe(T t, Integer x) {
		if (t instanceof List) {
			if (((List<?>) t).size() == 3)
				return true;
		}
		return false;
	}
}
