package com.examples.with.different.packagename.seeding;

public class C extends A {
	private boolean value = false;
	
	public void setFooBar(boolean value) {
		this.value = value;
		
	}
	@Override
	public boolean fooBar() {
		return value;
	}
}