package de.unisb.cs.st.evosuite.coverage.dataflow;

import org.apache.log4j.Logger;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import de.unisb.cs.st.evosuite.cfg.BytecodeInstruction;

/**
 * Abstract superclass for all Definitions and Uses 
 * 
 * @author Andre Mis
 */
public class DefUse extends BytecodeInstruction {

	private static Logger logger = Logger.getLogger(DefUse.class);
	
	protected boolean isParameterUse;
	int defuseId;
	int useId;
	int defId;
	
	// TODO equals() and ids and stuff
	
	// TODO decide casting versus this constructor approach - that in this specific case i weirdly like
	protected DefUse(BytecodeInstruction wrap, int defuseId, int defId, int useId, boolean isParameterUse) {
		super(wrap);
		if(!isDefUse())
			throw new IllegalArgumentException("only actual defuse instructions are accepted");
		
		this.defuseId = defuseId;
		this.defId = defId;
		this.useId = useId;
	}
	
	public boolean isParameterUse() {
		return isParameterUse;
	}
	
	public int getDefUseId() {
		return defuseId;
	}

	public int getUseId() {
		return useId;
	}

	public int getDefId() {
		return defId;
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
