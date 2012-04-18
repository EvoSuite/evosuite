package de.unisb.cs.st.evosuite.graphs.ccfg;

import de.unisb.cs.st.evosuite.graphs.cfg.BytecodeInstruction;

public class CCFGMethodCallNode extends CCFGNode {

	private BytecodeInstruction callInstruction;
	
	public CCFGMethodCallNode(BytecodeInstruction callInstruction) {
		this.callInstruction = callInstruction;
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

	@Override
	public String toString() {
		return "CALL from "+callInstruction.toString();
	}
}
