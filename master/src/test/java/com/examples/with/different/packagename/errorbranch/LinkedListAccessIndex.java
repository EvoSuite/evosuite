package com.examples.with.different.packagename.errorbranch;

import java.util.LinkedList;

public class LinkedListAccessIndex {

	public boolean testMe(LinkedList<Integer> list, int index, int value) {
		if(list.get(index) == value)
			return true;
		else
			return false;
	}
}
