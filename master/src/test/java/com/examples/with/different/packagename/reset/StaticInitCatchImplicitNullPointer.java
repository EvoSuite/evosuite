package com.examples.with.different.packagename.reset;

public class StaticInitCatchImplicitNullPointer {

	public boolean someMethod() {
		return false;
		
	}
	static {
		try {
			StaticInitCatchImplicitNullPointer obj = getSomeObject();
			obj.someMethod();
		} catch (NullPointerException e) {
			//expected
		}
	}
	private static StaticInitCatchImplicitNullPointer getSomeObject() {
		return null;
	}
}
