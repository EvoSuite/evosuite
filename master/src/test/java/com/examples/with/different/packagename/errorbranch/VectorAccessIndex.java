package com.examples.with.different.packagename.errorbranch;

import java.util.Vector;

public class VectorAccessIndex {

	public boolean testMe(Vector<Integer> list, int index, int value) {
		if(list.get(index) == value)
			return true;
		else
			return false;
	}
}
