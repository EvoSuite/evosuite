package de.unisb.cs.st.evosuite.coverage.dataflow;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.cfg.BytecodeInstruction;

/**
 * Abstract superclass for all Definitions and Uses 
 * 
 * @author Andre Mis
 */
public class DefUse extends BytecodeInstruction {

	private static Logger logger = Logger.getLogger(DefUse.class);
	
	int defuseId;
	int defId;
	int useId;
	boolean isParameterUse;
	
	
	protected DefUse(BytecodeInstruction wrap, int defuseId, int defId, int useId, boolean isParameterUse) {
		super(wrap);
		if(!isDefUse())
			throw new IllegalArgumentException("only actual defuse instructions are accepted");
		
		this.defuseId = defuseId;
		this.defId = defId;
		this.useId = useId;
		this.isParameterUse = isParameterUse;
	}
	
	public String getDUVariableType() {
		if(isFieldDU())
			return "Field";
		if(isParameterUse())
			return "Parameter";
		if(isLocalDU())
			return "Local";
		
		logger.warn("unexpected state");
		return "UNKNOWN";
	}
	
	// getter
	
	public int getDefUseId() {
		return defuseId;
	}

	public int getUseId() {
		return useId;
	}

	public int getDefId() {
		return defId;
	}
	
	public boolean isParameterUse() {
		return isParameterUse;
	}

	// inherited from Object
	
	@Override
	public boolean equals(Object obj) {
		if(this==obj)
			return true;
		if(obj==null)
			return false;
		if(obj instanceof DefUse) {
			DefUse other = (DefUse)obj;
			if(defuseId != other.defuseId)
				return false;
		}
		return super.equals(obj);
	}

	@Override
	public String toString() {
		StringBuilder r = new StringBuilder();
		if(isDefinition())
			r.append("Definition "+getDefId());
		if(isUse())
			r.append("Use "+getUseId());
		r.append(" for ");
		if(isStaticDefUse())
			r.append("static ");
		r.append(getDUVariableType());
		r.append("-Variable \"" + getDUVariableName() +"\"");
		r.append(" in " + getMethodName()+"."+getBytecodeId()); 
		r.append(" branch " + getBranchId() + (branchExpressionValue?"t":"f"));
		r.append(" line "+ getLineNumber());
		return r.toString();
	}
}
