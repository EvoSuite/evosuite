/**
 * 
 */
package com.examples.with.different.packagename.generic;

import java.util.List;

/**
 * @author Gordon Fraser
 * 
 */
public class GenericParameters5 {

	public boolean testMe(List<?> aList) {
		if (aList.isEmpty())
			return true;
		else
			return false;
	}

}
