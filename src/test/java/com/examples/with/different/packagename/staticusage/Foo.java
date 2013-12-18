package com.examples.with.different.packagename.staticusage;

public class Foo {

	static {
		Bar3.methodBar3();
	}
	
	public static void methodFoo1(){
		Bar1.methodBar1();
	}
	
	public static int methodFoo2() {
		int x = Bar2.fieldBar2;
		return x;
	}

	public static int methodFoo3() {
		int x = Bar4.methodBar4();
		return x;
	}

}
