package de.unisb.cs.st.evosuite.coverage.branch;

import de.unisb.cs.st.evosuite.cfg.CFGGenerator.CFGVertex;

public class Branch {

	CFGVertex v;
	
	public Branch(CFGVertex v) {
		if(!v.isBranch())
			throw new IllegalArgumentException("Vertex of a branch expected");
		
		this.v = v;
	}
	
	public CFGVertex getCFGVertex() {
		return v;
	}
	
	public int getBranchID() {
		return v.branchID;
	}
	
	public int getBytecodeID() {
		return v.getID();
	}
	
}
