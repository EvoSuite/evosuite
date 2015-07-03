package com.examples.with.different.packagename.concolic;

public class IntHolder {
	public int intValue;

	public IntHolder(int myInt) {
		this.intValue = myInt;
	}

	public int getValue() {
		return intValue;
	}
}