package de.unisb.cs.st.evosuite.graphs.ccg;

import de.unisb.cs.st.evosuite.graphs.ccfg.CCFGEdge;
import de.unisb.cs.st.evosuite.graphs.cfg.BytecodeInstruction;

public class ClassCallEdge extends CCFGEdge {

	private static final long serialVersionUID = 7136724698608115327L;

	private BytecodeInstruction callInstruction;
	
	public ClassCallEdge(BytecodeInstruction callInstruction) {
		this.callInstruction = callInstruction;
	}
	
	public BytecodeInstruction getCallInstruction() {
		return callInstruction;
	}
	
	@Override
	public String toString() {
		return callInstruction.toString();
	}
	
}
