package com.examples.with.different.packagename.purity;

public class InterfaceInspectorCalls {

	private InterfaceInspector iInspector1;
	private InterfaceInspector iInspector2;

	public InterfaceInspectorCalls(int x) {
		iInspector1 = new AllPureInspectors(x);
		iInspector2 = new PureImpureInspectors(x);
	}

	public boolean pureInspector1() {
		return iInspector1.pureInspector() == 0;
	}

	public boolean impureInspector1() {
		return iInspector1.impureInspector() == 0;
	}

	public boolean pureInspector2() {
		return iInspector2.pureInspector() == 0;
	}

	public boolean impureInspector2() {
		return iInspector2.impureInspector() == 0;
	}

}
