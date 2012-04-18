package de.unisb.cs.st.evosuite.graphs.ccfg;

public class CCFGFrameNode extends CCFGNode {

	private ClassControlFlowGraph.FrameNodeType type;
	
	public CCFGFrameNode(ClassControlFlowGraph.FrameNodeType type) {
		this.type = type;
	}
	
	public ClassControlFlowGraph.FrameNodeType getType() {
		return type;
	}

	@Override
	public String toString() {
		return "Frame "+type.toString();
	}
	
}
