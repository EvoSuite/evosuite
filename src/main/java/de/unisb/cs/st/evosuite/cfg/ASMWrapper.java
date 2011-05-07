package de.unisb.cs.st.evosuite.cfg;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;

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
	
	protected AbstractInsnNode node;
	
	
	protected CFGFrame frame; // TODO ???

	public boolean isJump() {
		return (node instanceof JumpInsnNode);
	}

	public boolean isGoto() {
		if (node instanceof JumpInsnNode) {
			return (node.getOpcode() == Opcodes.GOTO);
		}
		return false;
	}
	
	
	public String getMethodName() { // TODO ???
		return ((MethodInsnNode) node).name;
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
	 * TODO repair
	 * WARNING: throws ClassCastException on non-LineNumberNode node
	 *  
	 */
	public int getLineNumber() {
		return ((LineNumberNode)node).line;
	}

	public boolean isLabel() {
		return node instanceof LabelNode;
	}

	public boolean isReturn() {
		switch (node.getOpcode()) {
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
		if (node.getOpcode() == Opcodes.ATHROW) {
			// TODO: Need to check if this is a caught exception?
			return true;
		}
		return false;
	}

	public boolean isTableSwitch() {
		return (node instanceof TableSwitchInsnNode);
	}

	public boolean isLookupSwitch() {
		return (node instanceof LookupSwitchInsnNode);
	}



	public AbstractInsnNode getNode(){
		return node;
	}
	
	public boolean isBranchLabel() {
		if (node instanceof LabelNode
				&& ((LabelNode) node).getLabel().info instanceof Integer) {
			return true;
		}
		return false;
	}

	public boolean isLineNumber() {
		return (node instanceof LineNumberNode);
	}

//	public int getBranchId() {
//		// return ((Integer)((LabelNode)node).getLabel().info).intValue();
//		return line_no;
//	}

	public boolean isIfNull() {
		if (node instanceof JumpInsnNode) {
			return (node.getOpcode() == Opcodes.IFNULL);
		}
		return false;
	}

	public boolean isMethodCall() {
		return node instanceof MethodInsnNode;
	}

	/**
	 * 
	 * @param methodName
	 * @return
	 */
	public boolean isMethodCall(String methodName) {
		if (node instanceof MethodInsnNode) {
			MethodInsnNode mn = (MethodInsnNode) node;
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
		return node.equals(other.node);
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

	public boolean isLocalVarDefinition() {
		return node.getOpcode() == Opcodes.ISTORE
		|| node.getOpcode() == Opcodes.LSTORE
		|| node.getOpcode() == Opcodes.FSTORE
		|| node.getOpcode() == Opcodes.DSTORE
		|| node.getOpcode() == Opcodes.ASTORE
		|| node.getOpcode() == Opcodes.IINC;
	}

	public boolean isLocalVarUse() {
		return node.getOpcode() == Opcodes.ILOAD || node.getOpcode() == Opcodes.LLOAD
		|| node.getOpcode() == Opcodes.FLOAD
		|| node.getOpcode() == Opcodes.DLOAD
		|| node.getOpcode() == Opcodes.ALOAD
		|| node.getOpcode() == Opcodes.IINC;
	}

	public boolean isDefinition() {
		return isFieldDefinition() || isLocalVarDefinition();
	}

	public boolean isUse() {
		return isFieldUse() || isLocalVarUse();
	}

	public boolean isFieldDefinition() {
		return node.getOpcode() == Opcodes.PUTFIELD
		|| node.getOpcode() == Opcodes.PUTSTATIC;
	}

	public boolean isFieldUse() {
		return node.getOpcode() == Opcodes.GETFIELD
		|| node.getOpcode() == Opcodes.GETSTATIC;
	}

	public boolean isStaticDefUse() {
		return node.getOpcode() == Opcodes.PUTSTATIC
		|| node.getOpcode() == Opcodes.GETSTATIC;
	}
}
