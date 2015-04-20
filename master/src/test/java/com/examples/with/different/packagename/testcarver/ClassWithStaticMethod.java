package com.examples.with.different.packagename.testcarver;

public class ClassWithStaticMethod {

	private ClassWithStaticMethod() {
		
	}
	
	public static ClassWithStaticMethod getInstance() {
		return new ClassWithStaticMethod();
	}
	
	public boolean testMe(int x) {
		return x == 42;
	}
}
