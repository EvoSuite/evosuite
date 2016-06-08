package com.examples.with.different.packagename.clinit;

public class SimpleClass {

	static {
		for (int i = 0; i < 10 ; i++) {}
	}
	
	private int value;
	
	public SimpleClass() {}
	
	public void setValue(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return this.value;
	}
	
	public void clear() {
		value = 0;
	}
}
