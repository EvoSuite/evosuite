package de.unisb.cs.st.evosuite.graphs.ccfg;

import de.unisb.cs.st.evosuite.graphs.ccfg.CCFGEdge;
import de.unisb.cs.st.evosuite.graphs.cfg.BytecodeInstruction;

public class CCFGMethodCallEdge extends CCFGEdge {

	private static final long serialVersionUID = -1638791707105165885L;

	private BytecodeInstruction callInstruction;
	
	private boolean isCallingEdge;
	
	public CCFGMethodCallEdge(BytecodeInstruction callInstruction, boolean isCallingEdge) {
		this.callInstruction = callInstruction;
		this.isCallingEdge = isCallingEdge;
	}
	
	
	/**
	 * Marks whether this is a calling edge or a returning edge 
	 */
	public boolean isCallingEdge() {
		return isCallingEdge;
	}


	public BytecodeInstruction getCallInstruction() {
		return callInstruction;
	}

	@Override
	public String toString() {
		return (isCallingEdge?"calling ":"returning from ")+callInstruction.getCalledMethod();
	}
}
