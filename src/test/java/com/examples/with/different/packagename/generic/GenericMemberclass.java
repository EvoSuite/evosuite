/**
 * 
 */
package com.examples.with.different.packagename.generic;

/**
 * @author Gordon Fraser
 * 
 */
public class GenericMemberclass<T> {

	public class MemberClass {
		private final T t;

		public MemberClass(T t) {
			this.t = t;
		}

		public T getObject() {
			return t;
		}
	}

	public boolean testMe(MemberClass x, T t) {
		if (x.getObject() == t)
			return true;
		else
			return false;
	}
}
