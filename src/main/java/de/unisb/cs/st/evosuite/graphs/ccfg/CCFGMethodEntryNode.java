package de.unisb.cs.st.evosuite.graphs.ccfg;

public class CCFGMethodEntryNode extends CCFGNode {

	private String method;
	
	public CCFGMethodEntryNode(String method) {
		this.method = method;
	}
	
	@Override
	public String toString() {
		return "Entry: "+method;
	}
}
