package de.unisb.cs.st.evosuite.coverage.dataflow;

import de.unisb.cs.st.evosuite.cfg.CFGGenerator.CFGVertex;

public abstract class DefUse {

	CFGVertex v;
	
	public CFGVertex getCFGVertex() {
		return v;
	}

	public String getDUVariableName() {
		return v.getDUVariableName();
	}
	
	public int getDUID() {
		return v.duID;
	}
	
	public String getMethodName() {
		return v.methodName;
	}
	
	public String getClassName() {
		return v.className;
	}
	
	public int getBranchID() {
		return v.branchID;
	}
	
	public int getLineNumber() {
		return v.line_no;
	}
	
	public boolean isUse() {
		return v.isUse();
	}
	
	public boolean isDefinition() {
		return v.isDefinition();
	}	
}
