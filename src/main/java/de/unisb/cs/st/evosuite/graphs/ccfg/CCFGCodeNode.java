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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((codeInstruction == null) ? 0 : codeInstruction.hashCode());
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
		CCFGCodeNode other = (CCFGCodeNode) obj;
		if (codeInstruction == null) {
			if (other.codeInstruction != null)
				return false;
		} else if (!codeInstruction.equals(other.codeInstruction))
			return false;
		return true;
	}

	@Override
	public String toString() {
		if(codeInstruction.isMethodCall())
			return codeInstruction.toString()+" in class "+codeInstruction.getCalledMethodsClass();
		else
			return codeInstruction.toString();
	}
}
