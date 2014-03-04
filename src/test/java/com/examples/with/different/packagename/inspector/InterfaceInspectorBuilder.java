package com.examples.with.different.packagename.inspector;

public class InterfaceInspectorBuilder {

	public InterfaceInspector build(int x) {
		return new PureImpureInspectors(x);
	}
}
