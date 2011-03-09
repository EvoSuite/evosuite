package de.unisb.cs.st.evosuite.coverage.dataflow;

import de.unisb.cs.st.evosuite.coverage.CFGVertexHolder;

/**
 * Abstract superclass for all Definitions and Uses 
 * 
 * @author Andre Mis
 */
public abstract class DefUse extends CFGVertexHolder {


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
	
	public int getDUID() {
		return v.duID;
	}
	
	public int getDefID() {
		return v.defID;
	}
	
	public int getUseID() {
		return v.useID;
	}
	
	public boolean isUse() {
		return v.isUse();
	}
	
	public boolean isDefinition() {
		return v.isDefinition();
	}
	
	public boolean isStaticDU() {
		return v.isStaticDU();
	}
	
	public boolean isLocalDU() {
		return v.isLocalDU();
	}
	
	public String toString() {
		
		String s = "";
		
		if(isUse())
			s+="Use "+v.useID+" ";
		if(isDefinition())
			s+="Definition "+v.defID+" ";
		s+="duID "+getDUID()+" for var "+getDUVariableName();
		
		return s;
	}
}
