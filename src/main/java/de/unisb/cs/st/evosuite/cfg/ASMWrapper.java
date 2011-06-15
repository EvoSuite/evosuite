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
import org.objectweb.asm.util.AbstractVisitor;

// TODO: the following methods about control dependence are flawed right now:
//			- the BytecodeInstruction of a Branch does not have it's control dependent branchId
//				but it's own branchId set 
//			- this seems to be OK for ChromosomeRecycling as it stands, but
//				especially getControlDependentBranch() will fail hard when called on a Branch
//				the same may hold for the other ones as well. 
//			- look at BranchCoverageGoal and Branch for more information

/**
 * Wrapper class for the underlying byteCode instruction library ASM
 * 
 * Gives access to a lot of methods that interpret the raw information in
 * AbstractInsnNode to usable chunks of information, inside EvoSuite
 * 
 * This class is supposed to hide the ASM library from the rest of EvoSuite as
 * much as possible
 * 
 * After initialization, all information about byteCode instructions should be
 * accessible via the BytecodeInstruction-, DefUse- and BranchPool. Each of
 * those has data structures holding all BytecodeInstruction, DefUse and Branch
 * objects respectively created during initialization.
 * 
 * BytecodeInstruction directly extends ASMWrapper and is the first way to
 * instantiate an ASMWrapper. Branch and DefUse extend BytecodeInstruction,
 * where DefUse is abstract and Branch is not ("concrete"?) DefUse is further
 * extended by Definition and Use
 * 
 * @author Andre Mis
 */
public abstract class ASMWrapper {

	// from ASM library
	protected AbstractInsnNode asmNode;
	protected CFGFrame frame; // TODO find out what that is used for

	public boolean canReturnFromMethod() {
		return isReturn() || isThrow();
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (o == null) {
			return false;
		}
		if (!(o instanceof ASMWrapper)) {
			return false;
		}

		ASMWrapper other = (ASMWrapper) o;

		return asmNode.equals(other.asmNode);
	}

	public AbstractInsnNode getASMNode() {
		return asmNode;
	}

	public String getDUVariableName() {
		if (this.isFieldDU()) {
			return getFieldName();
		} else {
			return getLocalVarName();
		}
	}

	public abstract int getInstructionId();

	// methods for branch analysis

	public String getInstructionType() {

		if ((asmNode.getOpcode() >= 0) && (asmNode.getOpcode() < AbstractVisitor.OPCODES.length)) {
			return AbstractVisitor.OPCODES[asmNode.getOpcode()];
		}

		if (isLineNumber()) {
			return "LINE " + this.getLineNumber();
		}

		return getType();
	}

	public int getLineNumber() {
		if (!isLineNumber()) {
			return -1;
		}

		return ((LineNumberNode) asmNode).line;
	}

	// TODO unsafe
	public int getLocalVar() {
		if (asmNode instanceof VarInsnNode) {
			return ((VarInsnNode) asmNode).var;
		} else if (asmNode instanceof IincInsnNode) {
			return ((IincInsnNode) asmNode).var;
		} else {
			return -1;
		}
	}

	public abstract String getMethodName();

	public String getType() {
		// TODO explain
		String type = "";
		if ((asmNode.getType() >= 0) && (asmNode.getType() < AbstractVisitor.TYPES.length)) {
			type = AbstractVisitor.TYPES[asmNode.getType()];
		}

		return type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		// result = prime * result + getOuterType().hashCode();
		result = prime * result + getInstructionId();
		return result;
	}

	public boolean isActualBranch() {
		return isBranch() || isLookupSwitch() || isTableSwitch();
	}

	public boolean isBranch() {
		return isJump() && !isGoto();
	}

	public boolean isBranchLabel() {
		if ((asmNode instanceof LabelNode) && (((LabelNode) asmNode).getLabel().info instanceof Integer)) {
			return true;
		}
		return false;
	}

	public boolean isDefinition() {
		return isFieldDefinition() || isLocalVarDefinition();
	}

	// public int getBranchId() {
	// // return ((Integer)((LabelNode)node).getLabel().info).intValue();
	// return line_no;
	// }

	public boolean isDefUse() {
		return isLocalDU() || isFieldDU();
	}

	public boolean isFieldDefinition() {
		return (asmNode.getOpcode() == Opcodes.PUTFIELD) || (asmNode.getOpcode() == Opcodes.PUTSTATIC);
	}

	public boolean isFieldDU() {
		return isFieldDefinition() || isFieldUse();
	}

	// methods for defUse analysis

	public boolean isFieldUse() {
		return (asmNode.getOpcode() == Opcodes.GETFIELD) || (asmNode.getOpcode() == Opcodes.GETSTATIC);
	}

	public boolean isGoto() {
		if (asmNode instanceof JumpInsnNode) {
			return (asmNode.getOpcode() == Opcodes.GOTO);
		}
		return false;
	}

	public boolean isIfNull() {
		if (asmNode instanceof JumpInsnNode) {
			return (asmNode.getOpcode() == Opcodes.IFNULL);
		}
		return false;
	}

	public boolean isJump() {
		return (asmNode instanceof JumpInsnNode);
	}

	public boolean isLabel() {
		return asmNode instanceof LabelNode;
	}

	/**
	 * Determines if this instruction is a line number instruction
	 * 
	 * More precisely this method checks if the underlying asmNode is a
	 * LineNumberNode
	 */
	public boolean isLineNumber() {
		return (asmNode instanceof LineNumberNode);
	}

	public boolean isLocalDU() {
		return isLocalVarDefinition() || isLocalVarUse();
	}

	public boolean isLocalVarDefinition() {
		return (asmNode.getOpcode() == Opcodes.ISTORE) || (asmNode.getOpcode() == Opcodes.LSTORE)
				|| (asmNode.getOpcode() == Opcodes.FSTORE) || (asmNode.getOpcode() == Opcodes.DSTORE)
				|| (asmNode.getOpcode() == Opcodes.ASTORE) || (asmNode.getOpcode() == Opcodes.IINC);
	}

	// retrieving information about variable names from ASM

	public boolean isLocalVarUse() {
		return (asmNode.getOpcode() == Opcodes.ILOAD) || (asmNode.getOpcode() == Opcodes.LLOAD)
				|| (asmNode.getOpcode() == Opcodes.FLOAD) || (asmNode.getOpcode() == Opcodes.DLOAD)
				|| (asmNode.getOpcode() == Opcodes.IINC)
				|| ((asmNode.getOpcode() == Opcodes.ALOAD) && (getLocalVar() != 0)); // exclude
																						// ALOAD
																						// 0
																						// (this)
	}

	public boolean isLookupSwitch() {
		return (asmNode instanceof LookupSwitchInsnNode);
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
			// #TODO this is unsafe methods should be identified by a signature
			// not by a name
			return mn.name.equals(methodName);
		}
		return false;
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

	public boolean isStaticDefUse() {
		return (asmNode.getOpcode() == Opcodes.PUTSTATIC) || (asmNode.getOpcode() == Opcodes.GETSTATIC);
	}

	// other classification methods

	public boolean isTableSwitch() {
		return (asmNode instanceof TableSwitchInsnNode);
	}

	public boolean isThrow() {
		if (asmNode.getOpcode() == Opcodes.ATHROW) {
			// TODO: Need to check if this is a caught exception?
			return true;
		}
		return false;
	}

	public boolean isUse() {
		return isFieldUse() || isLocalVarUse();
	}

	// sanity checks

	public void sanityCheckAbstractInsnNode(AbstractInsnNode node) {
		if (node == null) {
			throw new IllegalArgumentException("null given");
		}
		if (!node.equals(this.asmNode)) {
			throw new IllegalStateException("sanity check failed");
		}
	}

	// inherited from Object

	protected String getFieldName() {
		return ((FieldInsnNode) asmNode).name;
	}

	protected String getLocalVarName() {
		return getMethodName() + "_LV_" + getLocalVar();
	}
}
