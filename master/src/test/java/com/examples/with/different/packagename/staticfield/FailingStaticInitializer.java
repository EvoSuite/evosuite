package com.examples.with.different.packagename.staticfield;

public class FailingStaticInitializer {
	
	private static FailingStaticInitializer instance;
	
	static {
		// this crashes the class loading
		instance.toString();
	}

}
