package de.unisb.cs.st.evosuite.coverage.dataflow;

import de.unisb.cs.st.evosuite.cfg.CFGGenerator.CFGVertex;
import de.unisb.cs.st.evosuite.coverage.CFGVertexHolder;

/**
 * Abstract superclass for all Definitions and Uses 
 * 
 * @author Andre Mis
 */
public abstract class DefUse extends CFGVertexHolder {


	public DefUse(CFGVertex v) {
		super(v);
	}

	public String getDUVariableType() {
		if(v.isFieldDU())
			return "Field";
		if(v.isParameterUse())
			return "Parameter";
		if(v.isLocalDU())
			return "Local";
		
		return "UNKNOWN";
	}	
	
	public int getLocalVarNr() {
		return v.getLocalVar();
	}
	
	public String getDUVariableName() {
		return v.getDUVariableName();
	}
	
	public int getDefUseId() {
		return v.defuseId;
	}
	
	public int getDefId() {
		return v.defId;
	}
	
	public int getUseId() {
		return v.useId;
	}
	
	public boolean isUse() {
		return v.isUse();
	}
	
	public boolean isDefinition() {
		return v.isDefinition();
	}
	
	public boolean isStaticDU() {
		return v.isStaticDefUse();
	}
	
	public boolean isLocalDU() {
		return v.isLocalDU();
	}
	
	public String toString() {
		StringBuilder r = new StringBuilder();
		if(isDefinition())
			r.append("Definition "+getDefId());
		if(isUse())
			r.append("Use "+getUseId());
		r.append(" for ");
		if(isStaticDU())
			r.append("static ");
		r.append(getDUVariableType());
		r.append("-Variable \"" + getDUVariableName() +"\"");
		r.append(" in " + getMethodName()+"."+getBytecodeId()); 
		r.append(" branch " + getBranchId() + (getCFGVertex().branchExpressionValue?"t":"f"));
		r.append(" line "+ getLineNumber());
		return r.toString();
	}
}
