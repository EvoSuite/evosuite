package de.unisb.cs.st.evosuite.graphs.ccfg;

public class CCFGFrameNode extends CCFGNode {

	public enum FrameNodeType {ENTRY, EXIT, LOOP, CALL, RETURN};
	
	private FrameNodeType type;
	
	public CCFGFrameNode(FrameNodeType type) {
		this.type = type;
	}
	
	@Override
	public String toString() {
		return "Frame "+type.toString();
	}
	
}
