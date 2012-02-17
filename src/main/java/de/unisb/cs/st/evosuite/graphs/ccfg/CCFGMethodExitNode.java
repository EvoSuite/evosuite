package de.unisb.cs.st.evosuite.graphs.ccfg;

public class CCFGMethodExitNode extends CCFGNode {

	private String method;
	
	public CCFGMethodExitNode(String method) {
		this.method = method;
	}
	
	@Override
	public String toString() {
		return "Exit: "+method;
	}
}
