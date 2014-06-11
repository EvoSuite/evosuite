/**
 * 
 */
package com.examples.with.different.packagename.generic;

/**
 * @author Gordon Fraser
 * 
 */
public class GenericClassWithGenericMethodAndSubclass<T> {

	public static class Foo<T> {
		private final T object;

		private Foo(T object) {
			this.object = object;
		}

		public T getObject() {
			return object;
		}
	}

	public final <S extends T> Foo<S> wrap(S object) {
		return new Foo<S>(object);
	}

	public boolean test(Foo<T> foo1, Foo<T> foo2) {
		if (foo1.getObject() == foo2.getObject())
			return true;
		else
			return false;
	}
}
