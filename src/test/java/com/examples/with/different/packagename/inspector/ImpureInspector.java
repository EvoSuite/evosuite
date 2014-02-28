package com.examples.with.different.packagename.inspector;

public class ImpureInspector {
	
	private int value;
	
	public int getPureValue() {
		return value;
	}
	
	public int getImpureValue() {
		value++;
		return value;
	}

}
