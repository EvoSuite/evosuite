package com.examples.with.different.packagename.generic;

public class GenericOnlyInMemberclass {

	public static final class Foo<T> {
		
		protected T t = null;
		
		public void set(T t) {
			this.t = t;
		}
		
		public T get() {
			return t;
		}
	}
}
