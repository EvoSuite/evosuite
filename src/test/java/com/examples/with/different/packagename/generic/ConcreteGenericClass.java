/**
 * 
 */
package com.examples.with.different.packagename.generic;

/**
 * @author Gordon Fraser
 * 
 */
public class ConcreteGenericClass<T> extends AbstractGenericClass<T> {

	private ConcreteGenericClass(int value) {
		super(value);
	}

	public static <S> ConcreteGenericClass<S> create(int value) {
		return new ConcreteGenericClass<S>(value);
	}
}
