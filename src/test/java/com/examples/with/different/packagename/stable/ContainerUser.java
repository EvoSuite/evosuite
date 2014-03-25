package com.examples.with.different.packagename.stable;

import java.util.LinkedList;
import java.util.List;

public class ContainerUser {

	private final List<Object> myList = new LinkedList<Object>();

	public ContainerUser() {
		myList.add(new Object());
	}

	public boolean isEmpty() {
		return myList.isEmpty();
	}

	public String toString() {
		boolean isEmptyFlag = myList.isEmpty();
		return "isEmpty(myList)=" + isEmptyFlag + " ,size(myList)"
				+ myList.size();
	}
}
