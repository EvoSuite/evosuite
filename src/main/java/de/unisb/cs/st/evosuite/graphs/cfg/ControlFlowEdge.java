package de.unisb.cs.st.evosuite.graphs.cfg;

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
	
//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = 1;
//		result = prime * result + ((cd == null) ? 0 : cd.hashCode());
//		result = prime * result + (isExceptionEdge ? 1231 : 1237);
//		return result;
//	}
//
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj)
//			return true;
//		if (obj == null)
//			return false;
//		if (getClass() != obj.getClass())
//			return false;
//		ControlFlowEdge other = (ControlFlowEdge) obj;
//		if (cd == null) {
//			if (other.cd != null)
//				return false;
//		} else if (!cd.equals(other.cd))
//			return false;
//		if (isExceptionEdge != other.isExceptionEdge)
//			return false;
//		return true;
//	}

	@Override
	public String toString() {
		String r = "";
		if(isExceptionEdge)
			 r+= "E ";
		if (cd != null)
			r += cd.toString();
		return r;
	}
}
