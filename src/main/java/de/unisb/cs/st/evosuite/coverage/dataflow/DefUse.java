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
	
	protected boolean isParameterUse=false;
	
	public int defuseId = -1;
	public int useId = -1;
	public int defId = -1;
	
	// TODO decide casting versus this constructor approach - that in this specific case i weirdly like
	public DefUse(BytecodeInstruction wrap) {
		// TODO manage ids
		super(wrap);
		if(!isDefUse())
			throw new IllegalArgumentException("only actual defuse instructions are accepted");
	}

	public boolean isParameterUse() {
		return isParameterUse;
	}
	
	public void setParameterUse(boolean isParameterUse) {
		this.isParameterUse = isParameterUse;
	}
	
	public int getDefUseId() {
		return defuseId;
	}

	public void setDefUseId(int defuseId) {
		this.defuseId = defuseId;
	}

	public int getUseId() {
		return useId;
	}

	public void setUseId(int useId) {
		this.useId = useId;
	}

	public int getDefId() {
		return defId;
	}

	public void setDefId(int defId) {
		this.defId = defId;
	}

	public String getFieldName() {
		return ((FieldInsnNode) asmNode).name;
	}

	// TODO make this "retrieveLocalVar"
	public int getLocalVar() {
		if (asmNode instanceof VarInsnNode)
			return ((VarInsnNode) asmNode).var;
		else
			return ((IincInsnNode) asmNode).var;
	}

	public String getLocalVarName() {
		return methodName + "_LV_" + getLocalVar();
	}

	public String getDUVariableName() {
		if (this.isFieldDU())
			return getFieldName();
		else
			return getLocalVarName();
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
