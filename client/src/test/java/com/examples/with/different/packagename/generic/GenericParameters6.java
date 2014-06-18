/**
 * 
 */
package com.examples.with.different.packagename.generic;

import java.util.List;

/**
 * @author Gordon Fraser
 * 
 */
public class GenericParameters6 {

	public boolean testMe(List<?> aList) {
		if (aList.contains("test"))
			return true;
		else
			return false;
	}
}
