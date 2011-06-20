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
	
	String varName; 
	
	
	protected DefUse(BytecodeInstruction wrap, int defuseId, int defId, int useId, boolean isParameterUse) {
		super(wrap);
		if(!isDefUse())
			throw new IllegalArgumentException("only actual defuse instructions are accepted");
		if (defuseId < 0)
			throw new IllegalArgumentException("expect defUseId to be positive");
		if(defId < 0 && useId < 0)
			throw new IllegalArgumentException("expect either defId or useId to be set");
		
		this.defuseId = defuseId;
		this.defId = defId;
		this.useId = useId;
		this.isParameterUse = isParameterUse;
		this.varName = super.getDUVariableName();
		if(this.varName == null)
			throw new IllegalStateException("expect defUses to have non-null varaible names");
	}
	
	/**
	 *  Determines whether the given BytecodeInstruction constitutes
	 * a Definition that can potentially become an active Definition for this DefUse
	 * 
	 * in the sense that if control flow passes through the instruction
	 * of the given Definition that Definition becomes  active for this DefUse's variable
	 * 
	 * This is the case if the given Definition defines the same variable as this DefUse
	 * So a Definition canBecomeActive for itself
	 */
	public boolean canBecomeActiveDefinition(BytecodeInstruction instruction) {
		if(!instruction.isDefinition())
			return false;
		
		Definition otherDef = DefUseFactory.makeDefinition(instruction);
		return sharesVariableWith(otherDef);
	}
	
	/**
	 *  Determines whether the given DefUse reads or writes the same variable
	 * as this DefUse 
	 */
	public boolean sharesVariableWith(DefUse du) {
		return varName.equals(du.varName);
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
	
	@Override
	public String getDUVariableName() {
		return varName;
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

		// TODO ensure those checks succeed by always having IDs set properly
		// s. super.equals() for similar prob
		
//		if(obj instanceof DefUse) {
//			DefUse other = (DefUse)obj;
//			if(defuseId != other.defuseId)
//				return false;
//		}
		
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
		r.append(" in " + getMethodName()+"."+getInstructionId()); 
		r.append(" branch " + getControlDependentBranchId() + (getControlDependentBranchExpressionValue()?"t":"f"));
		r.append(" line "+ getLineNumber());
		return r.toString();
	}
}
