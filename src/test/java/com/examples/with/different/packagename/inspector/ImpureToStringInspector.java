package com.examples.with.different.packagename.inspector;

public class ImpureToStringInspector {

	private int value;
	
	public ImpureToStringInspector(int value) {
		this.value = value;
	}
	
	private void inc() {
		value++;
	}
	
	@Override
	public String toString() {
		inc();
		return String.valueOf(value);
	}
	
}
