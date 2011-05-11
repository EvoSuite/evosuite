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
//			- the BytecodeInstruction of a Branch does not have it's control dependent branchId
//				but it's own branchId set 
//			- this seems to be OK for ChromosomeRecycling as it stands, but
//				especially getControlDependentBranch() will fail hard when called on a Branch
//				the same may hold for the other ones as well. 
//			- look at BranchCoverageGoal and Branch for more information


// TODO clean up

/**
 * Wrapper class for the underlying byteCode instruction library ASM
 * 
 *  Gives access to a lot of methods that interpret the raw information in AbstractInsnNode
 *  to usable chunks of information, inside EvoSuite
 *  
 * This class is supposed to hide the ASM library from the rest of EvoSuite as much as possible
 * 
 * After initialization, all information about byteCode instructions should be accessible via
 * the BytecodeInstruction-, DefUse- and BranchPool. Each of those has data structures holding
 * all BytecodeInstruction, DefUse and Branch objects respectively created during initialization.
 * 
 * BytecodeInstruction directly extends ASMWrapper and is the first way to instantiate an ASMWrapper.
 * Branch and DefUse extend BytecodeInstruction, where DefUse is abstract and Branch is not ("concrete"?)
 * DefUse is further extended by Definition and Use
 *  
 * @author Andre Mis
 */
public abstract class ASMWrapper {

	// identification of a byteCode instruction inside EvoSuite
	protected String className;
	protected String methodName;
	protected int instructionId;
	
	// auxilary information
	protected int lineNumber = -1;
	
	// from ASM library
	protected AbstractInsnNode asmNode;
	protected CFGFrame frame; // TODO find out what that is used for

	public boolean isJump() {
		return (asmNode instanceof JumpInsnNode);
	}

	public boolean isGoto() {
		if (asmNode instanceof JumpInsnNode) {
			return (asmNode.getOpcode() == Opcodes.GOTO);
		}
		return false;
	}
	
	
	public String getMethodName() { // TODO make like getLineNumber()
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
	 * If hasLineNumberSet() returns true, this method returns the lineNumber of this instruction
	 * Otherwise an IllegalStateException() will be thrown to indicate that the field was never
	 * initialized properly
	 * 
	 */
	public int getLineNumber() {
		if(!hasLineNumberSet())
			throw new IllegalStateException("expect hasLineNumberSet() to be true on a BytecodeInstruction that gets asked for it's lineNumber");
		
		return lineNumber;
	}
	
	/**
	 *  
	 */
	public void setLineNumber(int lineNumber) {
		if(lineNumber<=0)
			throw new IllegalArgumentException("expect lineNumber value to be positive");
		
		if(isLineNumber()) {
			int asmLine = ((LineNumberNode)asmNode).line;
			// sanity check
			if(lineNumber!= -1 && asmLine != lineNumber)
				throw new IllegalStateException("linenumber instruction has lineNumber field set to a value different from instruction linenumber");
			this.lineNumber = asmLine;
		} else {
			this.lineNumber = lineNumber; 
		}
	}

	/**
	 * At first, if this instruction constitutes a line number instruction
	 * this method tries to retrieve the lineNumber from the underlying asmNode
	 * and set the lineNumber field to the value given by the asmNode.
	 * 
	 * This can lead to an IllegalStateException, should the lineNumber field have been
	 * set to another value previously
	 * 
	 * After that, if the lineNumber field is still not initialized, this method returns false
	 * Otherwise it returns true
	 */
	public boolean hasLineNumberSet() {
		retrieveLineNumber();
		return lineNumber != -1;
	}
	
	/**
	 * If the underlying ASMNode is a LineNumberNode the lineNumber field of this instance
	 * will be set to the lineNumber contained in that LineNumberNode
	 * 
	 * Should the lineNumber field have been set to a value different from that contained
	 * in the asmNode, this method throws an IllegalStateExeption
	 */
	private void retrieveLineNumber() {
		if(isLineNumber()) {
			int asmLine = ((LineNumberNode)asmNode).line;
			// sanity check
			if(this.lineNumber!=-1 && asmLine!=this.lineNumber)
				throw new IllegalStateException("lineNumber field was manually set to a value different from the actual lineNumber contained in LineNumberNode");
			this.lineNumber = asmLine;
		}
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
	
	// inherited from Object
	
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
		if(o==this)
			return true;
		if(o==null)
			return false;
		if(!(o instanceof ASMWrapper))
			return false;
		
		ASMWrapper other = (ASMWrapper)o;
		
		if (instructionId != other.instructionId)
			return false;
		if (methodName != null && !methodName.equals(other.methodName))
			return false;
		if (className != null && !className.equals(other.className))
			return false;
		
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
		else if(asmNode instanceof IincInsnNode)
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
