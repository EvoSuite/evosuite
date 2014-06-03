package com.examples.with.different.packagename.errorbranch;

import java.util.LinkedList;

public class LinkedListAccess {

	public boolean testMe(LinkedList<Integer> list, int x) {
		if(list.getFirst() == x)
			return true;
		else
			return false;
	}
}
