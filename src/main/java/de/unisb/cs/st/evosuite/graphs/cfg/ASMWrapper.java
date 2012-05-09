package de.unisb.cs.st.evosuite.graphs.cfg;

import mockit.external.asm.Type;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.SourceValue;
import org.objectweb.asm.util.Printer;

import de.unisb.cs.st.evosuite.graphs.GraphPool;

// TODO: the following methods about control dependence are flawed right now:
// - the BytecodeInstruction of a Branch does not have it's control dependent
// branchId
// but it's own branchId set
// - this seems to be OK for ChromosomeRecycling as it stands, but
// especially getControlDependentBranch() will fail hard when called on a Branch
// the same may hold for the other ones as well.
// - look at BranchCoverageGoal and Branch for more information

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
	protected CFGFrame frame;
	protected boolean forcedBranch = false;

	public AbstractInsnNode getASMNode() {
		return asmNode;
	}

	public String getInstructionType() {

		if (asmNode.getOpcode() >= 0
				&& asmNode.getOpcode() < Printer.OPCODES.length)
			return Printer.OPCODES[asmNode.getOpcode()];

		if (isLineNumber())
			return "LINE " + this.getLineNumber();

		return getType();
	}

	public String getType() {
		// TODO explain
		String type = "";
		if (asmNode.getType() >= 0 && asmNode.getType() < Printer.TYPES.length)
			type = Printer.TYPES[asmNode.getType()];

		return type;
	}

	public abstract int getInstructionId();

	public abstract String getMethodName();

	// methods for branch analysis

	public boolean isActualBranch() {
		return isBranch() || isSwitch();
	}

	public boolean isSwitch() {
		return isLookupSwitch() || isTableSwitch();
	}

	public void forceBranch() {
		forcedBranch = true;
	}

	public boolean canReturnFromMethod() {
		return isReturn() || isThrow();
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

	public boolean isBranchLabel() {
		if (asmNode instanceof LabelNode
				&& ((LabelNode) asmNode).getLabel().info instanceof Integer) {
			return true;
		}
		return false;
	}

	public boolean isJump() {
		return (asmNode instanceof JumpInsnNode);
	}

	public boolean isGoto() {
		if (asmNode instanceof JumpInsnNode) {
			return (asmNode.getOpcode() == Opcodes.GOTO);
		}
		return false;
	}

	public boolean isBranch() {
		return (isJump() && !isGoto()) || forcedBranch;
	}

	// FIXXME: Andre will hate this
	public boolean isForcedBranch() {
		return forcedBranch;
	}

	// public int getBranchId() {
	// // return ((Integer)((LabelNode)node).getLabel().info).intValue();
	// return line_no;
	// }

	public boolean isIfNull() {
		if (asmNode instanceof JumpInsnNode) {
			return (asmNode.getOpcode() == Opcodes.IFNULL);
		}
		return false;
	}

	public boolean isFrame() {
		return asmNode instanceof FrameNode;
	}

	public boolean isMethodCall() {
		return asmNode instanceof MethodInsnNode;
	}

	/**
	 * Returns the conjunction of the name and method descriptor of the method
	 * called by this instruction
	 */
	public String getCalledMethod() {
		if (!isMethodCall())
			return null;
		MethodInsnNode meth = (MethodInsnNode) asmNode;
		return meth.name + meth.desc;
	}

	/**
	 * Returns true if and only if the class of the method called by this
	 * instruction is the same as the given className
	 */
	public boolean isMethodCallForClass(String className) {
		if (isMethodCall()) {
//			System.out.println("in isMethodCallForClass of "+toString()+" for arg "+className+" calledMethodsClass: "+getCalledMethodsClass()+" calledMethod "+getCalledMethod());
			return getCalledMethodsClass().equals(className);
		}
		return false;
	}

	/**
	 * Returns the class of the method called by this instruction
	 */
	public String getCalledMethodsClass() {
		if (isMethodCall()) {
			MethodInsnNode mn = (MethodInsnNode) asmNode;
			return mn.owner.replaceAll("/", "\\.");
		}
		return null;
	}

	/**
	 * Returns the number of arguments of the method called by this instruction
	 */
	public int getCalledMethodsArgumentCount() {
		if (isMethodCall()) {
			// int r = 0;
			MethodInsnNode mn = (MethodInsnNode) asmNode;
			Type[] argTypes = Type.getArgumentTypes(mn.desc);

			return argTypes.length;
			// for(Type argType : argTypes) {
			// r+=argType.getSize();
			// }
			// return r;
		}
		return -1;
	}

	public boolean isLoadConstant() {
		return asmNode.getOpcode() == Opcodes.LDC;
	}

	public boolean isConstant() {
		switch (asmNode.getOpcode()) {
		case Opcodes.LDC:
		case Opcodes.ICONST_0:
		case Opcodes.ICONST_1:
		case Opcodes.ICONST_2:
		case Opcodes.ICONST_3:
		case Opcodes.ICONST_4:
		case Opcodes.ICONST_5:
		case Opcodes.ICONST_M1:
		case Opcodes.LCONST_0:
		case Opcodes.LCONST_1:
		case Opcodes.DCONST_0:
		case Opcodes.DCONST_1:
		case Opcodes.FCONST_0:
		case Opcodes.FCONST_1:
		case Opcodes.FCONST_2:
		case Opcodes.BIPUSH:
		case Opcodes.SIPUSH:
			return true;
		default:
			return false;
		}
	}

	// methods for defUse analysis

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
		FieldInsnNode fieldNode = (FieldInsnNode) asmNode;
		return fieldNode.owner + "." + fieldNode.name;
		// return fieldNode.name;
	}

	protected String getLocalVarName() {
		return getMethodName() + "_LV_" + getLocalVar();
	}

	// TODO unsafe
	public int getLocalVar() {
		if (asmNode instanceof VarInsnNode)
			return ((VarInsnNode) asmNode).var;
		else if (asmNode instanceof IincInsnNode)
			return ((IincInsnNode) asmNode).var;
		else
			return -1;
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
				|| asmNode.getOpcode() == Opcodes.IINC
				|| (asmNode.getOpcode() == Opcodes.ALOAD && getLocalVar() != 0); // exclude
		// ALOAD_0
		// (this)
	}

	/**
	 * Determines whether this is the special ALOAD that pushes 'this' onto the
	 * stack
	 */
	public boolean isALOAD0() {
		return asmNode.getOpcode() == Opcodes.ALOAD && getLocalVar() == 0;
	}

	public boolean isDefinitionForVariable(String var) {
		return (isDefinition() && getDUVariableName().equals(var));
	}

	public boolean isInvokeSpecial() {
		return asmNode.getOpcode() == Opcodes.INVOKESPECIAL;
	}

	/**
	 * Checks whether this instruction is an INVOKESPECIAL instruction that
	 * calls a constructor.
	 */
	public boolean isConstructorInvocation() {
		if (!isInvokeSpecial())
			return false;

		MethodInsnNode invoke = (MethodInsnNode) asmNode;
		// if (!invoke.owner.equals(className.replaceAll("\\.", "/")))
		// return false;

		return invoke.name.equals("<init>");
	}

	/**
	 * Checks whether this instruction is an INVOKESPECIAL instruction that
	 * calls a constructor of the given class.
	 */
	public boolean isConstructorInvocation(String className) {
		if (!isInvokeSpecial())
			return false;

		MethodInsnNode invoke = (MethodInsnNode) asmNode;
		if (!invoke.owner.equals(className.replaceAll("\\.", "/")))
			return false;

		return invoke.name.equals("<init>");
	}

	// other classification methods

	/**
	 * Determines if this instruction is a line number instruction
	 * 
	 * More precisely this method checks if the underlying asmNode is a
	 * LineNumberNode
	 */
	public boolean isLineNumber() {
		return (asmNode instanceof LineNumberNode);
	}

	public int getLineNumber() {
		if (!isLineNumber())
			return -1;

		return ((LineNumberNode) asmNode).line;
	}

	public boolean isLabel() {
		return asmNode instanceof LabelNode;
	}

	// sanity checks

	public void sanityCheckAbstractInsnNode(AbstractInsnNode node) {
		if (node == null)
			throw new IllegalArgumentException("null given");
		if (!node.equals(this.asmNode))
			throw new IllegalStateException("sanity check failed for "
					+ node.toString() + " on " + getMethodName() + toString());
	}

}
