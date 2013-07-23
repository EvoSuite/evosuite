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
public class GenericMethodAlternativeBounds {

	public static enum Foo {

	}

	public static <K extends Enum<K>> List<K> create(Class<K> keyType) {
		List<K> list = new ArrayList<K>();
		return list;
	}
}
