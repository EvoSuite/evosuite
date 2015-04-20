package com.examples.with.different.packagename.testcarver;

public abstract class AbstractSuperClassWithFields {

	protected int x;
	
	public AbstractSuperClassWithFields(int x) {
		this.x = x;
	}
	
	public abstract boolean testMe(int y);
	
}
