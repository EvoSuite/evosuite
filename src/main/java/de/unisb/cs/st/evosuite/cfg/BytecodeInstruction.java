package de.unisb.cs.st.evosuite.cfg;

import java.io.Serializable;
import java.util.Set;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import de.unisb.cs.st.evosuite.coverage.branch.Branch;
import de.unisb.cs.st.evosuite.coverage.branch.BranchPool;

/**
 * Internal representation of a BytecodeInstruction
 * 
 * Extends ASMWrapper which serves as an interface to the ASM library.
 * 
 * Known super classes are DefUse and Branch which yield specific functionality
 * needed to achieve theirs respective coverage criteria
 * 
 * Old: Node of the control flow graph
 * 
 * @author Gordon Fraser, Andre Mis
 * 
 */
public class BytecodeInstruction extends ASMWrapper implements Serializable {

	private static final long serialVersionUID = 3630449183355518857L;

	// identification of a byteCode instruction inside EvoSuite
	protected String className;
	protected String methodName;
	protected int instructionId;
	protected int jpfId;

	// auxiliary information
	private int lineNumber = -1;

	// experiment: also searching through all CFG nodes in order to determine an
	// instruction BasicBlock might be a little to expensive too just to safe
	// space for one reference
	private BasicBlock basicBlock;

	/**
	 * Generates a ByteCodeInstruction instance that represents a byteCode
	 * instruction as indicated by the given ASMNode in the given method and
	 * class
	 */
	public BytecodeInstruction(String className, String methodName, int instructionId,
	        int jpfId, AbstractInsnNode asmNode) {

		if (className == null || methodName == null || asmNode == null)
			throw new IllegalArgumentException("null given");
		if (instructionId < 0)
			throw new IllegalArgumentException(
			        "expect instructionId to be positive, not " + instructionId);

		this.instructionId = instructionId;
		this.jpfId = jpfId;
		this.asmNode = asmNode;

		setClassName(className);
		setMethodName(methodName);
	}

	/**
	 * Can represent any byteCode instruction
	 */
	public BytecodeInstruction(BytecodeInstruction wrap) {

		this(wrap.className, wrap.methodName, wrap.instructionId, wrap.jpfId,
		        wrap.asmNode, wrap.lineNumber, wrap.basicBlock);
		this.forcedBranch = wrap.forcedBranch;
	}

	public BytecodeInstruction(String className, String methodName, int instructionId,
	        int jpfId, AbstractInsnNode asmNode, int lineNumber, BasicBlock basicBlock) {

		this(className, methodName, instructionId, jpfId, asmNode, lineNumber);

		this.basicBlock = basicBlock;
	}

	public BytecodeInstruction(String className, String methodName, int instructionId,
	        int jpfId, AbstractInsnNode asmNode, int lineNumber) {

		this(className, methodName, instructionId, jpfId, asmNode);

		if (lineNumber != -1)
			setLineNumber(lineNumber);
	}

	// getter + setter

	private void setMethodName(String methodName) {
		if (methodName == null)
			throw new IllegalArgumentException("null given");

		this.methodName = methodName;
	}

	private void setClassName(String className) {
		if (className == null)
			throw new IllegalArgumentException("null given");

		this.className = className;
	}

	// --- Field Management ---

	@Override
	public int getInstructionId() {
		return instructionId;
	}

	public int getJPFId() {
		return jpfId;
	}

	@Override
	public String getMethodName() {
		return methodName;
	}

	public String getClassName() {
		return className;
	}

	public String getName() {
		return "BytecodeInstruction " + instructionId + " in " + methodName;
	}

	/**
	 * Return's the BasicBlock that contain's this instruction in it's CFG.
	 * 
	 * If no BasicBlock containing this instruction was created yet, null is
	 * returned.
	 */
	public BasicBlock getBasicBlock() {
		if (!hasBasicBlockSet())
			retrieveBasicBlock();
		return basicBlock;
	}

	private void retrieveBasicBlock() {

		if (basicBlock == null)
			basicBlock = getActualCFG().getBlockOf(this);
	}

	/**
	 * Once the CFG has been asked for this instruction's BasicBlock it sets
	 * this instance's internal basicBlock field.
	 */
	public void setBasicBlock(BasicBlock block) {
		if (block == null)
			throw new IllegalArgumentException("null given");

		if (!block.getClassName().equals(getClassName())
		        || !block.getMethodName().equals(getMethodName()))
			throw new IllegalArgumentException(
			        "expect block to be for the same method and class as this instruction");
		if (this.basicBlock != null)
			throw new IllegalArgumentException(
			        "basicBlock already set! not allowed to overwrite");

		this.basicBlock = block;
	}

	/**
	 * Checks whether this instance's basicBlock has already been set by the CFG
	 * or
	 */
	public boolean hasBasicBlockSet() {
		return basicBlock != null;
	}

	@Override
	public int getLineNumber() {

		if (lineNumber == -1 && isLineNumber())
			retrieveLineNumber();

		return lineNumber;
	}

	/**
	 *  
	 */
	public void setLineNumber(int lineNumber) {
		if (lineNumber <= 0)
			throw new IllegalArgumentException("expect lineNumber value to be positive");

		if (isLabel())
			return;

		if (isLineNumber()) {
			int asmLine = super.getLineNumber();
			// sanity check
			if (lineNumber != -1 && asmLine != lineNumber)
				throw new IllegalStateException(
				        "linenumber instruction has lineNumber field set to a value different from instruction linenumber");
			this.lineNumber = asmLine;
		} else {
			this.lineNumber = lineNumber;
		}
	}

	/**
	 * At first, if this instruction constitutes a line number instruction this
	 * method tries to retrieve the lineNumber from the underlying asmNode and
	 * set the lineNumber field to the value given by the asmNode.
	 * 
	 * This can lead to an IllegalStateException, should the lineNumber field
	 * have been set to another value previously
	 * 
	 * After that, if the lineNumber field is still not initialized, this method
	 * returns false Otherwise it returns true
	 */
	public boolean hasLineNumberSet() {
		retrieveLineNumber();
		return lineNumber != -1;
	}

	/**
	 * If the underlying ASMNode is a LineNumberNode the lineNumber field of
	 * this instance will be set to the lineNumber contained in that
	 * LineNumberNode
	 * 
	 * Should the lineNumber field have been set to a value different from that
	 * contained in the asmNode, this method throws an IllegalStateExeption
	 */
	private void retrieveLineNumber() {
		if (isLineNumber()) {
			int asmLine = super.getLineNumber();
			// sanity check
			if (this.lineNumber != -1 && asmLine != this.lineNumber)
				throw new IllegalStateException(
				        "lineNumber field was manually set to a value different from the actual lineNumber contained in LineNumberNode");
			this.lineNumber = asmLine;
		}
	}

	// --- graph section ---

	/**
	 * Returns the ActualControlFlowGraph of this instructions method
	 * 
	 * Convenience method. Redirects the call to CFGPool.getActualCFG()
	 */
	public ActualControlFlowGraph getActualCFG() {

		ActualControlFlowGraph myCFG = CFGPool.getActualCFG(className, methodName);
		if (myCFG == null)
			throw new IllegalStateException(
			        "expect CFGPool to know CFG for every method for which an instruction is known");

		return myCFG;
	}

	/**
	 * Returns the RawControlFlowGraph of this instructions method
	 * 
	 * Convenience method. Redirects the call to CFGPool.getRawCFG()
	 */
	public RawControlFlowGraph getRawCFG() {

		RawControlFlowGraph myCFG = CFGPool.getRawCFG(className, methodName);
		if (myCFG == null)
			throw new IllegalStateException(
			        "expect CFGPool to know CFG for every method for which an instruction is known");

		return myCFG;
	}

	/**
	 * Returns the ControlDependenceGraph of this instructions method
	 * 
	 * Convenience method. Redirects the call to CFGPool.getCDG()
	 */
	public ControlDependenceGraph getCDG() {

		ControlDependenceGraph myCDG = CFGPool.getCDG(className, methodName);
		if (myCDG == null)
			throw new IllegalStateException(
			        "expect CFGPool to know CDG for every method for which an instruction is known");

		return myCDG;
	}

	// --- CDG-Section ---

	/**
	 * Returns a cfg.Branch object for each branch this instruction is control
	 * dependent on as determined by the ControlDependenceGraph. If this
	 * instruction is only dependent on the root branch this method returns an
	 * empty set
	 * 
	 * If this instruction is a Branch and it is dependent on itself - which can
	 * happen in loops for example - the returned set WILL contain this. If you
	 * do not need the full set in order to avoid loops, call
	 * getAllControlDependentBranches instead
	 */
	public Set<ControlDependency> getControlDependencies() {

		BasicBlock myBlock = getBasicBlock();

		// return new
		// HashSet<ControlDependency>(myBlock.getControlDependencies());
		return myBlock.getControlDependencies();
	}

	/**
	 * This method returns a random Branch among all Branches this instruction
	 * is control dependent on
	 * 
	 * If this instruction is only dependent on the root branch, this method
	 * returns null
	 * 
	 * Since EvoSuite was previously unable to detect multiple control
	 * dependencies for one instruction this method serves as a backwards
	 * compatibility bridge
	 */
	public Branch getControlDependentBranch() {

		Set<ControlDependency> controlDependentBranches = getControlDependencies();

		for (ControlDependency cd : controlDependentBranches)
			return cd.getBranch();

		return null; // root branch
	}

	/**
	 * Returns all branchIds of Branches this instruction is directly control
	 * dependent on as determined by the ControlDependenceGraph for this
	 * instruction's method.
	 * 
	 * If this instruction is control dependent on the root branch the id -1
	 * will be contained in this set
	 */
	public Set<Integer> getControlDependentBranchIds() {

		BasicBlock myBlock = getBasicBlock();

		return myBlock.getControlDependentBranchIds();
	}

	/**
	 * Determines whether or not this instruction is control dependent on the
	 * root branch of it's method by calling getControlDependentBranchIds() to
	 * see if the return contains -1.
	 */
	public boolean isRootBranchDependent() {
		return getControlDependencies().isEmpty();
	}

	/**
	 * This method returns a random branchId among all branchIds this
	 * instruction is control dependent on.
	 * 
	 * This method returns -1 if getControlDependentBranch() returns null,
	 * otherwise that Branch's branchId is returned
	 * 
	 * Note: The returned branchExpressionValue comes from the same Branch
	 * getControlDependentBranch() and getControlDependentBranchId() return
	 * 
	 * Since EvoSuite was previously unable to detect multiple control
	 * dependencies for one instruction this method serves as a backwards
	 * compatibility bridge
	 */
	public int getControlDependentBranchId() {

		Branch b = getControlDependentBranch();
		if (b == null)
			return -1;

		return b.getActualBranchId();
	}

	/**
	 * This method returns the branchExpressionValue from a random Branch among
	 * all Branches this instruction is control dependent on.
	 * 
	 * This method returns true if getControlDependentBranch() returns null,
	 * otherwise that Branch's branchExpressionValue is returned
	 * 
	 * Note: The returned branchExpressionValue comes from the same Branch
	 * getControlDependentBranch() and getControlDependentBranchId() return
	 * 
	 * Since EvoSuite was previously unable to detect multiple control
	 * dependencies for one instruction this method serves as a backwards
	 * compatibility bridge
	 */
	public boolean getControlDependentBranchExpressionValue() {

		Branch b = getControlDependentBranch();
		return getBranchExpressionValue(b);
	}

	public boolean getBranchExpressionValue(Branch b) {
		if (!isDirectlyControlDependentOn(b))
			throw new IllegalArgumentException(
			        "this method can only be called for branches that this instruction is directly control dependent on.");

		if (b == null)
			return true; // root branch special case

		return getControlDependency(b).getBranchExpressionValue();
	}

	/**
	 * Determines whether this BytecodeInstruction is directly control dependent
	 * on the given Branch. Meaning within this instruction CDG there is an
	 * incoming ControlFlowEdge to this instructions BasicBlock holding the
	 * given Branch as it's branchInstruction.
	 * 
	 * If the given Branch is null, this method checks whether the this
	 * instruction is control dependent on the root branch of it's method.
	 */
	public boolean isDirectlyControlDependentOn(Branch branch) {
		if (branch == null)
			return getControlDependentBranchIds().contains(-1);

		for (ControlDependency cd : getControlDependencies())
			if (cd.getBranch().equals(branch))
				return true;

		return false;
	}

	public ControlDependency getControlDependency(Branch branch) {
		if (!isDirectlyControlDependentOn(branch))
			throw new IllegalArgumentException(
			        "instruction not directly control dependent on given branch");

		for (ControlDependency cd : getControlDependencies())
			if (cd.getBranch().equals(branch))
				return cd;

		throw new IllegalStateException(
		        "expect getControlDependencies() to contain a CD for each branch that isDirectlyControlDependentOn() returns true on");
	}

	// /**
	// * WARNING: better don't user this method right now TODO
	// *
	// * Determines whether the CFGVertex is transitively control dependent on
	// the
	// * given Branch
	// *
	// * A CFGVertex is transitively control dependent on a given Branch if the
	// * Branch and the vertex are in the same method and the vertex is either
	// * directly control dependent on the Branch - look at
	// * isDirectlyControlDependentOn(Branch) - or the CFGVertex of the control
	// * dependent branch of this CFGVertex is transitively control dependent on
	// * the given branch.
	// *
	// */
	// public boolean isTransitivelyControlDependentOn(Branch branch) {
	// if (!getClassName().equals(branch.getClassName()))
	// return false;
	// if (!getMethodName().equals(branch.getMethodName()))
	// return false;
	//
	// // TODO: this method does not take into account, that there might be
	// // multiple branches this instruction is control dependent on
	//
	// BytecodeInstruction vertexHolder = this;
	// do {
	// if (vertexHolder.isDirectlyControlDependentOn(branch))
	// return true;
	// vertexHolder = vertexHolder.getControlDependentBranch()
	// .getInstruction();
	// } while (vertexHolder != null);
	//
	// return false;
	// }

	// /**
	// * WARNING: better don't user this method right now TODO
	// *
	// * Determines the number of branches that have to be passed in order to
	// pass
	// * this CFGVertex
	// *
	// * Used to determine TestFitness difficulty
	// */
	// public int getCDGDepth() {
	//
	// // TODO: this method does not take into account, that there might be
	// // multiple branches this instruction is control dependent on
	//
	// Branch current = getControlDependentBranch();
	// int r = 1;
	// while (current != null) {
	// r++;
	// current = current.getInstruction().getControlDependentBranch();
	// }
	// return r;
	// }

	// String methods

	public String explain() {
		if (isBranch()) {
			if (BranchPool.isKnownAsBranch(this)) {
				Branch b = BranchPool.getBranchForInstruction(this);
				if (b == null)
					throw new IllegalStateException(
					        "expect BranchPool to be able to return Branches for instructions fullfilling BranchPool.isKnownAsBranch()");

				return "Branch " + b.getActualBranchId() + " - " + getInstructionType();
			}
			return "UNKNOWN Branch I" + instructionId + " " + getInstructionType();

			// + " - " + ((JumpInsnNode) asmNode).label.getLabel();
		}

		return getASMNodeString();
	}

	public String getASMNodeString() {
		String type = getType();
		String opcode = getInstructionType();

		String stack = "";
		if (frame == null)
			stack = "null";
		else
			for (int i = 0; i < frame.getStackSize(); i++) {
				stack += frame.getStack(i) + ",";
			}

		if (asmNode instanceof LabelNode) {
			return "LABEL " + ((LabelNode) asmNode).getLabel().toString();
		} else if (asmNode instanceof FieldInsnNode)
			return "Field" + " " + ((FieldInsnNode) asmNode).owner + "."
			        + ((FieldInsnNode) asmNode).name + " Type=" + type + ", Opcode="
			        + opcode;
		else if (asmNode instanceof FrameNode)
			return "Frame" + " " + asmNode.getOpcode() + " Type=" + type + ", Opcode="
			        + opcode;
		else if (asmNode instanceof IincInsnNode)
			return "IINC " + ((IincInsnNode) asmNode).var + " Type=" + type + ", Opcode="
			        + opcode;
		else if (asmNode instanceof InsnNode)
			return "" + opcode;
		else if (asmNode instanceof IntInsnNode)
			return "INT " + ((IntInsnNode) asmNode).operand + " Type=" + type
			        + ", Opcode=" + opcode;
		else if (asmNode instanceof MethodInsnNode)
			return opcode + " " + ((MethodInsnNode) asmNode).name;
		else if (asmNode instanceof JumpInsnNode)
			return "JUMP " + ((JumpInsnNode) asmNode).label.getLabel() + " Type=" + type
			        + ", Opcode=" + opcode + ", Stack: " + stack + " - Line: "
			        + lineNumber;
		else if (asmNode instanceof LdcInsnNode)
			return "LDC " + ((LdcInsnNode) asmNode).cst + " Type=" + type; // +
		// ", Opcode=";
		// + opcode; // cst starts with mutationid if
		// this is location of mutation
		else if (asmNode instanceof LineNumberNode)
			return "LINE " + " " + ((LineNumberNode) asmNode).line;
		else if (asmNode instanceof LookupSwitchInsnNode)
			return "LookupSwitchInsnNode" + " " + asmNode.getOpcode() + " Type=" + type
			        + ", Opcode=" + opcode;
		else if (asmNode instanceof MultiANewArrayInsnNode)
			return "MULTIANEWARRAY " + " " + asmNode.getOpcode() + " Type=" + type
			        + ", Opcode=" + opcode;
		else if (asmNode instanceof TableSwitchInsnNode)
			return "TableSwitchInsnNode" + " " + asmNode.getOpcode() + " Type=" + type
			        + ", Opcode=" + opcode;
		else if (asmNode instanceof TypeInsnNode)
			return "NEW " + ((TypeInsnNode) asmNode).desc;
		// return "TYPE " + " " + node.getOpcode() + " Type=" + type
		// + ", Opcode=" + opcode;
		else if (asmNode instanceof VarInsnNode)
			return opcode + " " + ((VarInsnNode) asmNode).var;
		else
			return "Unknown node" + " Type=" + type + ", Opcode=" + opcode;
	}

	// --- Inherited from Object ---

	@Override
	public String toString() {

		String r = "I" + instructionId;

		r += " " + explain();

		if (hasLineNumberSet() && !isLineNumber())
			r += " l" + getLineNumber();

		return r;
	}

	// @Override
	// public boolean equals(Object obj) {
	// if (this == obj)
	// return true;
	// if (obj == null)
	// return false;
	// if (!(obj instanceof BytecodeInstruction))
	// return false;
	//
	// // TODO ensure that the following checks always succeed
	// // TODO do this by ensuring that those values are always set correctly
	//
	// BytecodeInstruction other = (BytecodeInstruction) obj;
	//
	// if (instructionId != other.instructionId)
	// return false;
	// if (methodName != null && !methodName.equals(other.methodName))
	// return false;
	// if (className != null && !className.equals(other.className))
	// return false;
	//
	// return super.equals(obj);
	// }

	/**
	 * Convenience method:
	 * 
	 * If this instruction is known by the BranchPool to be a Branch, you can
	 * call this method in order to retrieve the corresponding Branch object
	 * registered within the BranchPool.
	 * 
	 * Otherwise this method will return null;
	 */
	public Branch toBranch() {

		try {
			return BranchPool.getBranchForInstruction(this);
		} catch (Exception e) {
			return null;
		}
	}

	public boolean proceedsConstructorInvocation() {

		RawControlFlowGraph cfg = getRawCFG();
		for (BytecodeInstruction other : cfg.vertexSet())
			if (other.isConstructorInvocation())
				if (getInstructionId() < other.getInstructionId())
					return true;

		return false;
	}

	public boolean isWithinConstructor() {
		return getMethodName().startsWith("<init>");
	}

	public boolean isLastInstructionInMethod() {
		return equals(getRawCFG().getInstructionWithBiggestId());
	}

	public boolean canBeExitPoint() {
		return canReturnFromMethod() || isLastInstructionInMethod();
	}
}