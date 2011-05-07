package de.unisb.cs.st.evosuite.cfg;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

// TODO: the following methods about control dependence are flawed right now:
//			- the CFGVertex of a Branch does not have it's control dependent branchId
//				but it's own branchId set 
//			- this seems to be OK for ChromosomeRecycling as it stands, but
//				especially getControlDependentBranch() will fail hard when called on a Branch
//				the same may hold for the other ones as well. 
//			- look at BranchCoverageGoal and Branch for more information

// TODO ensure only one AbstractInsnNode object for each byteCode instruction 
//			.. wrappers may get cloned but not the nodes themselves!

/**
 * Convenience-superclass for classes that hold a BytecodeInstruction
 * 
 * Just gives direct access to a lot of methods from the CFGVertex
 * Known subclasses are Branch and DefUse 
 * 
 * @author Andre Mis
 */
public abstract class ASMWrapper {

	protected String className;
	protected String methodName;
	protected int instructionId;
	protected int lineNumber = -1;
	
	// from ASM library
	protected AbstractInsnNode asmNode;
	protected CFGFrame frame; // TODO ???

	public boolean isJump() {
		return (asmNode instanceof JumpInsnNode);
	}

	public boolean isGoto() {
		if (asmNode instanceof JumpInsnNode) {
			return (asmNode.getOpcode() == Opcodes.GOTO);
		}
		return false;
	}
	
	
	public String getMethodName() { // TODO ???
		return ((MethodInsnNode) asmNode).name;
	}
	
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public boolean isBranch() {
		return isJump() && !isGoto();
	}
	
	public boolean isActualBranch() {
		return isBranch() 
				|| isLookupSwitch() 
				|| isTableSwitch();
	}

	
	/**
	 * If lineNumber was set previously that value is returned.
	 * Otherwise set previously set line Number is returned.
	 * 
	 * If this wraps a LineNumberNode, the line field
	 * of asmNode is set as lineNumber.
	 */
	public int getLineNumber() {
		if(isLineNumber() && lineNumber == -1)
			lineNumber = ((LineNumberNode)asmNode).line; 
		
		return lineNumber;
	}

	public boolean isLabel() {
		return asmNode instanceof LabelNode;
	}

	public boolean isReturn() {
		switch (asmNode.getOpcode()) {
		case Opcodes.RETURN:
		case Opcodes.ARETURN:
		case Opcodes.IRETURN:
		case Opcodes.LRETURN:
		case Opcodes.DRETURN:
		case Opcodes.FRETURN:
			return true;
		default:
			return false;
		}
	}

	public boolean isThrow() {
		if (asmNode.getOpcode() == Opcodes.ATHROW) {
			// TODO: Need to check if this is a caught exception?
			return true;
		}
		return false;
	}

	public boolean isTableSwitch() {
		return (asmNode instanceof TableSwitchInsnNode);
	}

	public boolean isLookupSwitch() {
		return (asmNode instanceof LookupSwitchInsnNode);
	}



	public AbstractInsnNode getASMNode(){
		return asmNode;
	}
	
	public boolean isBranchLabel() {
		if (asmNode instanceof LabelNode
				&& ((LabelNode) asmNode).getLabel().info instanceof Integer) {
			return true;
		}
		return false;
	}

	public boolean isLineNumber() {
		return (asmNode instanceof LineNumberNode);
	}

//	public int getBranchId() {
//		// return ((Integer)((LabelNode)node).getLabel().info).intValue();
//		return line_no;
//	}

	public boolean isIfNull() {
		if (asmNode instanceof JumpInsnNode) {
			return (asmNode.getOpcode() == Opcodes.IFNULL);
		}
		return false;
	}

	public boolean isMethodCall() {
		return asmNode instanceof MethodInsnNode;
	}

	/**
	 * 
	 * @param methodName
	 * @return
	 */
	public boolean isMethodCall(String methodName) {
		if (asmNode instanceof MethodInsnNode) {
			MethodInsnNode mn = (MethodInsnNode) asmNode;
			//#TODO this is unsafe methods should be identified by a signature not by a name
			return mn.name.equals(methodName);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		// result = prime * result + getOuterType().hashCode();
		result = prime * result + instructionId;
		return result;
	}
	
	@Override
	public boolean equals(Object o) {
		// TODO
		if(o==this)
			return true;
		if(o==null)
			return false;
		if(!(o instanceof ASMWrapper))
			return false;
		ASMWrapper other = (ASMWrapper)o;
		return asmNode.equals(other.asmNode);
	}
	
	public boolean isDefUse() {
		return isLocalDU() || isFieldDU();
	}

	public boolean isFieldDU() {
		return isFieldDefinition() || isFieldUse();
	}

	public boolean isLocalDU() {
		return isLocalVarDefinition() || isLocalVarUse();
	}

	public boolean isDefinition() {
		return isFieldDefinition() || isLocalVarDefinition();
	}

	public boolean isUse() {
		return isFieldUse() || isLocalVarUse();
	}

	public boolean isFieldDefinition() {
		return asmNode.getOpcode() == Opcodes.PUTFIELD
		|| asmNode.getOpcode() == Opcodes.PUTSTATIC;
	}

	public boolean isFieldUse() {
		return asmNode.getOpcode() == Opcodes.GETFIELD
		|| asmNode.getOpcode() == Opcodes.GETSTATIC;
	}

	public boolean isStaticDefUse() {
		return asmNode.getOpcode() == Opcodes.PUTSTATIC
		|| asmNode.getOpcode() == Opcodes.GETSTATIC;
	}
	
	// retrieving information about variable names from ASM
	
	public String getDUVariableName() {
		if (this.isFieldDU())
			return getFieldName();
		else
			return getLocalVarName();
	}

	protected String getFieldName() {
		return ((FieldInsnNode) asmNode).name;
	}
	
	protected String getLocalVarName() {
		return methodName + "_LV_" + getLocalVar();
	}
	
	// TODO unsafe
	public int getLocalVar() {
		if (asmNode instanceof VarInsnNode)
			return ((VarInsnNode) asmNode).var;
		else
			return ((IincInsnNode) asmNode).var;
	}
	
	public boolean isLocalVarDefinition() {
		return asmNode.getOpcode() == Opcodes.ISTORE
				|| asmNode.getOpcode() == Opcodes.LSTORE
				|| asmNode.getOpcode() == Opcodes.FSTORE
				|| asmNode.getOpcode() == Opcodes.DSTORE
				|| asmNode.getOpcode() == Opcodes.ASTORE
				|| asmNode.getOpcode() == Opcodes.IINC;
	}

	public boolean isLocalVarUse() {
		return asmNode.getOpcode() == Opcodes.ILOAD
				|| asmNode.getOpcode() == Opcodes.LLOAD
				|| asmNode.getOpcode() == Opcodes.FLOAD
				|| asmNode.getOpcode() == Opcodes.DLOAD
				|| asmNode.getOpcode() == Opcodes.ALOAD
				|| asmNode.getOpcode() == Opcodes.IINC;
	}

	// sanity checks
	
	public void sanityCheckAbstractInsnNode(AbstractInsnNode node) {
		if(node == null)
			throw new IllegalArgumentException("null given");
		if(!node.equals(this.asmNode))
			throw new IllegalStateException("sanity check failed");
	}
}
