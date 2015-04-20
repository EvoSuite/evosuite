/**
 * 
 */
package com.examples.with.different.packagename.generic;

import java.util.LinkedList;

/**
 * @author Gordon Fraser
 * 
 */
@SuppressWarnings("rawtypes")
public class GenericWithPartialParameters extends LinkedList<Comparable> {

	private static final long serialVersionUID = -1542079183619543446L;

	public <E extends Comparable> boolean testMe(E foo) {
		if (contains(foo))
			return true;
		else
			return false;
	}
}
