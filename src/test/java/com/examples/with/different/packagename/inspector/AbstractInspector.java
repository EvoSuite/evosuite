package com.examples.with.different.packagename.inspector;

public abstract class AbstractInspector {

	
	
	public int negateValue(int x) {
		return -x;
	}

	public int impureNegateValue(int value) {
		notPureGreaterThanZero();
		return -value;
	}
	
	public abstract boolean notPureGreaterThanZero();

}
