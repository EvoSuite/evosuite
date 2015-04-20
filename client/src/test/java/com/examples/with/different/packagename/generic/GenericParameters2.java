/**
 * 
 */
package com.examples.with.different.packagename.generic;

import java.util.List;

/**
 * @author Gordon Fraser
 * 
 */
public class GenericParameters2 {

	public boolean testMe1(List<String> stringList) {
		if (stringList.size() == 2)
			return true;
		else
			return false;
	}
}
