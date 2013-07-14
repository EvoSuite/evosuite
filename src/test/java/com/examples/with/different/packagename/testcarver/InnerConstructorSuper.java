package com.examples.with.different.packagename.testcarver;

public class InnerConstructorSuper {

	private boolean foo;
	
	public InnerConstructorSuper(boolean aBoolean){
		foo = aBoolean;
	}
	
	public String getFoo(){
		return "foo="+foo;
	}
}
