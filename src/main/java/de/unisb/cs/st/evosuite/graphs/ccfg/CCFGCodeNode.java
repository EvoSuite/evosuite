package de.unisb.cs.st.evosuite.graphs.ccfg;

import de.unisb.cs.st.evosuite.graphs.cfg.BytecodeInstruction;

public class CCFGCodeNode extends CCFGNode {

	private BytecodeInstruction code;
	
	public CCFGCodeNode(BytecodeInstruction code) {
		this.code = code;
	}
	
	public BytecodeInstruction getCode() {
		return code;
	}

	@Override
	public String toString() {
		return code.toString();
	}
}
