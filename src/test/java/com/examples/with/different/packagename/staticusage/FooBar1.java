package com.examples.with.different.packagename.staticusage;

public class FooBar1 {

	public static int myMethod() {
		int val = FooBar2.used_int_field;
		return val;
	}
	
}
