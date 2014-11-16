package com.examples.with.different.packagename.purity;

public class AllPureInspectors implements InterfaceInspector {

	private final int value;
	public AllPureInspectors(int value) {
		this.value = value;
	}
	
	@Override
	public int pureInspector() {
		return value;
	}

	@Override
	public int impureInspector() {
		return pureInspector();
	}

}
