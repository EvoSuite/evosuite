package de.unisb.cs.st.evosuite.graphs.ccfg;

import de.unisb.cs.st.evosuite.graphs.cfg.BytecodeInstruction;

public class CCFGMethodCallNode extends CCFGNode {

	private BytecodeInstruction callInstruction;
	private CCFGMethodReturnNode returnNode;
	
	public CCFGMethodCallNode(BytecodeInstruction callInstruction, CCFGMethodReturnNode returnNode) {
		this.callInstruction = callInstruction;
		this.returnNode = returnNode;
	}
	
	public String getMethod() {
		return callInstruction.getMethodName();
	}
	
	public String  getCalledMethod() {
		return callInstruction.getCalledMethod();
	}
	
	public BytecodeInstruction getCallInstruction() {
		return callInstruction;
	}
	
	public CCFGMethodReturnNode getReturnNode() {
		return returnNode;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((callInstruction == null) ? 0 : callInstruction.hashCode());
		result = prime * result
				+ ((returnNode == null) ? 0 : returnNode.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CCFGMethodCallNode other = (CCFGMethodCallNode) obj;
		if (callInstruction == null) {
			if (other.callInstruction != null)
				return false;
		} else if (!callInstruction.equals(other.callInstruction))
			return false;
		if (returnNode == null) {
			if (other.returnNode != null)
				return false;
		} else if (!returnNode.equals(other.returnNode))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CALL from "+callInstruction.toString();
	}
}
