package de.unisb.cs.st.evosuite.graphs.ccfg;

import de.unisb.cs.st.evosuite.graphs.cfg.BytecodeInstruction;

public class CCFGMethodReturnNode extends CCFGNode {

	private BytecodeInstruction callInstruction;
	
	public CCFGMethodReturnNode(BytecodeInstruction callInstruction) {
		this.callInstruction = callInstruction;
	}
	
	public BytecodeInstruction getCallInstruction() {
		return callInstruction;
	}
	
	@Override
	public String toString() {
		return "RETURN "+callInstruction.getCalledMethod();
	}
	
}
