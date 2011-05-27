package de.unisb.cs.st.evosuite.cfg;

import org.jgrapht.graph.DefaultEdge;

public class ControlFlowEdge extends DefaultEdge {

	private static final long serialVersionUID = -5009449930477928101L;

	private BytecodeInstruction branchInstruction = null;
	private boolean branchExpressionValue = true;

	public ControlFlowEdge() {

	}

	/**
	 * Sort of a copy constructor
	 */
	public ControlFlowEdge(ControlFlowEdge clone) {
		if(clone != null) {
			this.branchInstruction = clone.branchInstruction;
			this.branchExpressionValue = clone.branchExpressionValue;
		}
	}

	@Override
	public String toString() {

		String r = "";

		if (branchInstruction != null) {
			r += branchInstruction.toString();
			if (branchExpressionValue)
				r += " - TRUE";
			else
				r += " - FALSE";
		} 
//		else
//			r += "nonBranch";

//		r += "CFE";

		return r;
	}

	public BytecodeInstruction getBranchInstruction() {
		return branchInstruction;
	}

	public void setBranchInstruction(BytecodeInstruction branchInstruction) {
		this.branchInstruction = branchInstruction;
	}

	public boolean getBranchExpressionValue() {
		return branchExpressionValue;
	}

	public void setBranchExpressionValue(boolean branchExpressionValue) {
		this.branchExpressionValue = branchExpressionValue;
	}
}
