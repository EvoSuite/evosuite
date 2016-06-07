package com.examples.with.different.packagename.staticfield;

public class SimpleClass {

	private int myField;
	
	public SimpleClass() {}
	
	public void setField(int value) {
		this.myField = value;
	}
	
	public int getField() {
		return myField;
	}
	
	public void clearValue() {
		myField = 0;
	}
	
}
