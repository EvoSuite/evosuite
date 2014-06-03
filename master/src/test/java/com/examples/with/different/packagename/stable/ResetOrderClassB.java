package com.examples.with.different.packagename.stable;

public class ResetOrderClassB {

	public static final ResetOrderClassB OBJECT;
	static {
		System.out.println("enter B.<clinit>()");
		OBJECT = new ResetOrderClassB();
		System.out.println("exit B.<clinit>()");
	}

}
