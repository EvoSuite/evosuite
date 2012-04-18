package de.unisb.cs.st.evosuite.graphs.ccfg;

import de.unisb.cs.st.evosuite.graphs.cfg.BytecodeInstruction;

public class CCFGCodeNode extends CCFGNode {

	private BytecodeInstruction codeInstruction;
	
	public CCFGCodeNode(BytecodeInstruction code) {
		this.codeInstruction = code;
	}
	
	public String getMethod() {
		return codeInstruction.getMethodName();
	}
	
	public BytecodeInstruction getCodeInstruction() {
		return codeInstruction;
	}

	@Override
	public String toString() {
		return codeInstruction.toString();
	}
}
