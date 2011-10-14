package de.unisb.cs.st.evosuite.cfg;

import org.jgrapht.graph.DefaultEdge;

import de.unisb.cs.st.evosuite.coverage.branch.Branch;

public class ControlFlowEdge extends DefaultEdge {

	private static final long serialVersionUID = -5009449930477928101L;

	private ControlDependency cd;
	private boolean isExceptionEdge;

	public ControlFlowEdge() {
		this.cd = null;
		this.isExceptionEdge = false;
	}

	public ControlFlowEdge(boolean isExceptionEdge) {
		this.isExceptionEdge = isExceptionEdge;
	}
	
	public ControlFlowEdge(ControlDependency cd, boolean isExceptionEdge) {
		this.cd = cd;
		this.isExceptionEdge = isExceptionEdge;
	}
	

	/**
	 * Sort of a copy constructor
	 */
	public ControlFlowEdge(ControlFlowEdge clone) {
		if(clone != null) {
			this.cd = clone.cd;
			this.isExceptionEdge = clone.isExceptionEdge;
		}
	}

	@Override
	public String toString() {

		String r = "";
		
		if(isExceptionEdge)
			 r+= "E ";
		
		if (cd != null)
			r += cd.toString();

		return r;
	}
	
	public ControlDependency getControlDependency() {
		return cd;
	}

	public boolean hasControlDependency() {
		return cd != null;
	}
	
	public Branch getBranchInstruction() {
		if(cd == null)
			return null;
		
		return cd.getBranch();
	}
	
	public boolean isExceptionEdge() {
		return isExceptionEdge;
	}

	public boolean getBranchExpressionValue() {
		if(hasControlDependency())
			return cd.getBranchExpressionValue();
		
		return true;
	}
	
}
