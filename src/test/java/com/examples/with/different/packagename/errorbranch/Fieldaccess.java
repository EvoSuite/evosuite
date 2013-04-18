package com.examples.with.different.packagename.errorbranch;

public class Fieldaccess {

	private String foo;
	
	public Fieldaccess(String x) {
		foo = x;
	}
	
	public void testMe() {
		foo.toString();
	}
	
}
