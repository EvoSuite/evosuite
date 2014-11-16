package com.examples.with.different.packagename.errorbranch;

import java.util.Vector;

public class VectorAccess {
	public boolean testMe(Vector<Integer> list, int x) {
		if(list.firstElement() == x)
			return true;
		else
			return false;
	}
}
