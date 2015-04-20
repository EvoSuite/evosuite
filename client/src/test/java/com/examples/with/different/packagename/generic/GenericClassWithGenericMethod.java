/**
 * 
 */
package com.examples.with.different.packagename.generic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Gordon Fraser
 * 
 */
public class GenericClassWithGenericMethod<K> {
	public <K1 extends K> List<K1> build(Collection<? super K1> loader) {
		List<K1> list = new ArrayList<K1>();
		return list;
	}
}
