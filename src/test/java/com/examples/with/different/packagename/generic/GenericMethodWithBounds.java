package com.examples.with.different.packagename.generic;

import java.util.LinkedList;
import java.util.List;

public class GenericMethodWithBounds {

	public <T extends Comparable<T>> List<T> is(T element) {
		List<T> list = new LinkedList<T>();
		if(element != null) {
			list.add(element);
		}
		return list;
	}

}
