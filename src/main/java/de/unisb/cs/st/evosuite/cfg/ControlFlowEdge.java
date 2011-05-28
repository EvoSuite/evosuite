package de.unisb.cs.st.evosuite.cfg;

import org.jgrapht.graph.DefaultEdge;

import de.unisb.cs.st.evosuite.coverage.branch.Branch;
import de.unisb.cs.st.evosuite.coverage.branch.BranchPool;

public class ControlFlowEdge extends DefaultEdge {

	private static final long serialVersionUID = -5009449930477928101L;

	private Branch branchInstruction = null;
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

	public boolean hasBranchInstructionSet() {
		return branchInstruction != null;
	}
	
	public Branch getBranchInstruction() {
		return branchInstruction;
	}

	public void setBranchInstruction(BytecodeInstruction branchInstruction) {
		
		if (!branchInstruction.isActualBranch())
			throw new IllegalArgumentException(
					"expect given instruction to be an actual branch");
		
		Branch b = BranchPool.getBranchForInstruction(branchInstruction);
		if(b==null)
			throw new IllegalArgumentException("expect given instruction to be known to BranchPool");
		
		this.branchInstruction = b;
	}

	public boolean getBranchExpressionValue() {
		return branchExpressionValue;
	}

	public void setBranchExpressionValue(boolean branchExpressionValue) {
		
		if (branchInstruction == null)
			throw new IllegalStateException(
					"expect branchExpressionValue only to be set if branchInstruction was set previously");
		
		this.branchExpressionValue = branchExpressionValue;
	}
}
