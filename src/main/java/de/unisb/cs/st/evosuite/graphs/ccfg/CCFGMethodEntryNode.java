package de.unisb.cs.st.evosuite.graphs.ccfg;

public class CCFGMethodEntryNode extends CCFGNode {

	private String method;
	private CCFGCodeNode entryInstruction;
	
	public CCFGMethodEntryNode(String method, CCFGCodeNode entryInstruction) {
		this.method = method;
		this.entryInstruction = entryInstruction;
	}
	
	public String getMethod() {
		return method;
	}

	public CCFGCodeNode getEntryInstruction() {
		return entryInstruction;
	}
	
	@Override
	public String toString() {
		return "Entry: "+method;
	}
}
