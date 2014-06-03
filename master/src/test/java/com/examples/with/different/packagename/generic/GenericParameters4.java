/**
 * 
 */
package com.examples.with.different.packagename.generic;

import java.util.List;

/**
 * @author Gordon Fraser
 * 
 */
public class GenericParameters4 {

	public boolean testMe1(List<Integer> intList) {
		if (intList.size() > 0) {
			return true;
		}

		return false;
	}

	public boolean testMe2(List<String> stringList) {
		if (stringList.size() > 0) {
			return true;
		}
		return false;
	}
}
