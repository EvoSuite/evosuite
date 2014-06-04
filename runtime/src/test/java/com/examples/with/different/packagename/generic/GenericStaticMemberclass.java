package com.examples.with.different.packagename.generic;

public class GenericStaticMemberclass<T> {


	public static class MemberClass<T> {
		private final T t;

		public MemberClass(T t) {
			this.t = t;
		}

		public T getObject() {
			return t;
		}
	}

	public boolean testMe(MemberClass<T> x, T t) {
		if (x.getObject().equals(t))
			return true;
		else
			return false;
	}
	
}
