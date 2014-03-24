package com.examples.with.different.packagename.stable;

import java.util.LinkedList;
import java.util.List;

public class ContainerUser {

	private final List<Object> myList;

	public ContainerUser() {
		myList = new LinkedList<Object>();
		Object new_object = new Object();
		myList.add(new_object);
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
