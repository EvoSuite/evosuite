package de.unisb.cs.st.evosuite.cfg;

import org.jgrapht.graph.DefaultEdge;

public class ControlFlowEdge extends DefaultEdge {

	private static final long serialVersionUID = -5009449930477928101L;


	private BytecodeInstruction branchInstruction = null;
	private boolean branchExpressionValue = true;
	
	@Override
	public String toString() {
		
		String r = "";
		
		if(branchInstruction != null) {
			if(branchExpressionValue)
				r+= "TRUE-";
			else
				r+= "FALSE-";
			r += "_"+branchInstruction.toString();
		} else
			r += "nonBranch-";
		
		r += "CFE";
		
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
