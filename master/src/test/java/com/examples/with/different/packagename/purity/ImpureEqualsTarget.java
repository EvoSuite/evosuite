package com.examples.with.different.packagename.purity;

public class ImpureEqualsTarget {

	public ImpureEquals build(int x) {
		return new ImpureEquals(x);
	}

	public int process(ImpureEquals x) {
		int value = x.getValue();
		return value+10;
	}
	
	public ImpureEquals clone(ImpureEquals x) {
		return build(x.getValue());
	}

	
}
