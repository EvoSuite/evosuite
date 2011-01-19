package de.unisb.cs.st.evosuite.coverage;

import de.unisb.cs.st.evosuite.cfg.CFGGenerator.CFGVertex;

/**
 * Convenience-superclass for classes that hold a CFGVertex.
 * 
 * Just gives direct access to a lot of methods from the CFGVertex
 * Known subclasses are Branch and DefUse 
 * 
 * @author Andre Mis
 */
public abstract class CFGVertexHolder {

	protected CFGVertex v;
	
	public CFGVertex getCFGVertex() {
		return v;
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
	
	public int getBytecodeID() {
		return v.getID();
	}
}
