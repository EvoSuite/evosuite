/**
 * 
 */
package com.examples.with.different.packagename;

import java.util.Map;

/**
 * @author Gordon Fraser
 * 
 */
public class GenericParameters3 {

	public boolean testMe(Map<String, String> stringMap) {
		if (stringMap.containsKey("test"))
			return true;
		else
			return false;
	}
}
