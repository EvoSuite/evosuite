/**
 * 
 */
package com.examples.with.different.packagename.generic;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gordon Fraser
 * 
 */
public class GenericVarArgMethod {

	public static <E extends Number> List<E> of(E e1, E e2, E... others) {
		List<E> list = new ArrayList<E>();
		list.add(e1);
		list.add(e2);
		for (E e : others)
			list.add(e);
		return list;
	}
}
