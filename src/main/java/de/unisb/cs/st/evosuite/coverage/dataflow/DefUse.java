package de.unisb.cs.st.evosuite.coverage.dataflow;

import de.unisb.cs.st.evosuite.coverage.CFGVertexHolder;

/**
 * Abstract superclass for all Definitions and Uses 
 * 
 * @author Andre Mis
 */
public abstract class DefUse extends CFGVertexHolder {


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
