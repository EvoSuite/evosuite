package de.unisb.cs.st.evosuite.graphs.ccfg;

import de.unisb.cs.st.evosuite.graphs.cfg.BytecodeInstruction;

public class CCFGMethodCallNode extends CCFGNode {

	private BytecodeInstruction callInstruction;
	
	public CCFGMethodCallNode(BytecodeInstruction callInstruction) {
		this.callInstruction = callInstruction;
	}
	
	public BytecodeInstruction getCallInstruction() {
		return callInstruction;
	}

	@Override
	public String toString() {
		return "CALL "+callInstruction.getCalledMethod();
	}
	
}
