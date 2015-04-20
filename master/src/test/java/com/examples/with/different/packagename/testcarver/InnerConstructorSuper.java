package com.examples.with.different.packagename.testcarver;

public abstract class InnerConstructorSuper {

	private boolean foo;
	
	public InnerConstructorSuper(boolean aBoolean){
		super();
		foo = aBoolean;
	}
	
	public String getFoo(){
		return "foo="+foo;
	}
}
