/**
 * 
 */
package com.examples.with.different.packagename.generic;

import java.util.LinkedList;
import java.util.Map;

/**
 * @author Gordon Fraser
 * 
 */
public class GenericParameterExtendingGenericBounds<T extends Map<String, ?>> extends
        LinkedList<T> {

	private static final long serialVersionUID = -5120901091724267526L;

	public boolean testMe() {
		if (size() == 2)
			return true;
		else
			return false;
	}

}
