package com.examples.with.different.packagename.purity;

public class PureImpureInspectors implements InterfaceInspector {

	private int value;

	public PureImpureInspectors(int x) {
		this.value = x;
	}

	@Override
	public int pureInspector() {
		return value;
	}

	@Override
	public int impureInspector() {
		impureMethod();
		return value;
	}

	private void impureMethod() {
		value++;
	}
}
