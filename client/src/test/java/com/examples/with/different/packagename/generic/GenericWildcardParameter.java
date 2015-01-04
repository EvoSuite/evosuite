/**
 * 
 */
package com.examples.with.different.packagename.generic;

import java.util.List;

/**
 * @author Gordon Fraser
 * 
 */
public class GenericWildcardParameter {

	public boolean testMe(List<?> list) {
		if (list.size() == 2)
			return true;
		else
			return false;
	}
}
