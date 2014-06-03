/**
 * 
 */
package com.examples.with.different.packagename.generic;

import java.util.Map;

/**
 * @author Gordon Fraser
 * 
 */
public class GenericParameters3 {

	public boolean testMe(Map<String, String> stringMap, String key) {
		if (stringMap.containsKey(key))
			return true;
		else
			return false;
	}
}
