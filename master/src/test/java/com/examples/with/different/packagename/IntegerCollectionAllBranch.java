/**
 * 
 */
package com.examples.with.different.packagename;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gordon Fraser
 * 
 */
public class IntegerCollectionAllBranch {

	public boolean testMe(List<Integer> aList) {
		List<Integer> anotherList = new ArrayList<Integer>();

		anotherList.add(17);
		anotherList.add(34);

		if (aList.containsAll(anotherList))
			return true;
		else
			return false;
	}
}
