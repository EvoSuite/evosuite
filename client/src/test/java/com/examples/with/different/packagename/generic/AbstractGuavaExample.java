/**
 * 
 */
package com.examples.with.different.packagename.generic;

/**
 * @author Gordon Fraser
 * 
 */
public abstract class AbstractGuavaExample<T> {

	public <S extends T> Wrapper<S> wrap(S reference) {
		return new Wrapper<S>(this, reference);
	}

	public static AbstractGuavaExample<Object> identity() {
		return null;
	}

	public static class Wrapper<T> {
		private final AbstractGuavaExample<? super T> equivalence;
		private final T reference;

		private Wrapper(AbstractGuavaExample<? super T> equivalence, T reference) {
			this.equivalence = equivalence;
			this.reference = reference;
		}
	}
}
