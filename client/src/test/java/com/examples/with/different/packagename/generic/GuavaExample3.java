/**
 * 
 */
package com.examples.with.different.packagename.generic;

/**
 * @author Gordon Fraser
 * 
 */
public class GuavaExample3<R, C, V> {

	public static <R, C, V> GuavaExample3<R, C, V> create(
	        GuavaExample3<R, C, ? extends V> table) {
		GuavaExample3<R, C, V> result = new GuavaExample3<R, C, V>();
		return result;
	}
}
