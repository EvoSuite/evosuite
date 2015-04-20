package com.examples.with.different.packagename.purity;

public class InterfaceInspectorBuilder {

	public InterfaceInspector build(int x) {
		return new PureImpureInspectors(x);
	}
}
