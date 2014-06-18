/**
 * 
 */
package com.examples.with.different.packagename.generic;

import java.util.List;

/**
 * @author Gordon Fraser
 * 
 */
public class GenericParameters1 {

	public boolean testMe1(List<Integer> intList) {
		if (!intList.isEmpty()) {
			Integer y = intList.get(0);
			if (y == 10)
				return true;
		}

		return false;
	}
}
