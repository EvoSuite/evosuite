package de.unisb.cs.st.evosuite.cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import de.unisb.cs.st.evosuite.mutation.Mutateable;

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
public class BytecodeInstruction extends ASMWrapper implements Mutateable {

	// identification of a byteCode instruction inside EvoSuite
	protected String className;
	protected String methodName;
	protected int instructionId;

	// auxiliary information
	protected int lineNumber = -1;

	// --- - General - ---
	private int globalBytecodeInstructionId; // TODO

	// --- - Mutations - ---
	// Calculate distance to each mutation
	private Map<Long, Integer> mutant_distance = new HashMap<Long, Integer>();
	private final List<Long> mutations = new ArrayList<Long>();
	private boolean mutationBranch = false;
	private boolean mutatedBranch = false;

	// -- - Coverage Criteria - ---
	private int controlDependentBranchId = -1;
	// TODO branchExpressionValue should be false whenever it is true and visa
	// versa
	public boolean branchExpressionValue = true;

	// TODO: every BytecodeInstruction should
	// ..hold a reference to it's control dependent Branch
	// ..actually a CFGVertex should hold a
	// ..set of all branches it is control dependent on:
	// private Set<Branch> controlDependencies = new HashSet<Branch>();

	// TODO make sure the word CFGVertex appears nowhere anymore

	/**
	 * Can represent any byteCode instruction
	 */
	public BytecodeInstruction(BytecodeInstruction wrap) {

		this(wrap.className, wrap.methodName, wrap.instructionId, wrap.asmNode,
				wrap.lineNumber, wrap.controlDependentBranchId,
				wrap.branchExpressionValue);
	}

	public BytecodeInstruction(String className, String methodName,
			int instructionId, AbstractInsnNode asmNode, int lineNumber,
			int branchId, boolean branchExpressionValue) {

		this(className, methodName, instructionId, asmNode, lineNumber);

		this.controlDependentBranchId = branchId;
		this.branchExpressionValue = branchExpressionValue;
	}

	public BytecodeInstruction(String className, String methodName,
			int instructionId, AbstractInsnNode asmNode, int lineNumber) {

		this(className, methodName, instructionId, asmNode);
		if (lineNumber != -1)
			setLineNumber(lineNumber);
	}

	public BytecodeInstruction(String className, String methodName,
			int instructionId, AbstractInsnNode asmNode) {

		if (className == null || methodName == null || asmNode == null)
			throw new IllegalArgumentException("null given");
		if (instructionId < 0)
			throw new IllegalArgumentException(
					"expect instructionId to be positive, not " + instructionId);

		this.instructionId = instructionId;
		this.asmNode = asmNode;

		setClassName(className);
		setMethodName(methodName);
	}

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

	// --- Field Management --- TODO find out which ones to hide/remove

	// TODO merge with getId()!
	// TODO make real getId()!
	// TODO merge with getVertexId() ... oh boy
	// TODO merge with getBytecodeId()

	public int getId() {
		return instructionId;
	}

	public int getBytecodeId() {
		return instructionId;
	}

	public int getVertexId() {
		return instructionId;
	}

	public int getInstructionId() {
		return instructionId;
	}

	public String getMethodName() {
		return methodName;
	}

	public String getClassName() {
		return className;
	}

	public String getName() {
		return "BytecodeInstruction " + instructionId + " in " + methodName;
	}

	// mutation part

	public boolean isMutation() {
		return !mutations.isEmpty();
		/*
		 * if(node instanceof LdcInsnNode) {
		 * 
		 * if(((LdcInsnNode)node).cst.toString().contains("mutationId")) {
		 * logger.info("!!!!! Found mutation!"); } } return false;
		 */
	}

	public List<Long> getMutationIds() {
		return mutations;
		// String ids = ((LdcInsnNode)node).cst.toString();
		// return Integer.parseInt(ids.substring(ids.indexOf("_")+1));
	}

	public boolean isMutationBranch() {
		return isBranch() && mutationBranch;
	}

	public void setMutationBranch() {
		mutationBranch = true;
	}

	public void setMutatedBranch() {
		mutatedBranch = true;
	}

	public void setMutation(long id) {
		mutations.add(id);
	}

	public boolean hasMutation(long id) {
		return mutations.contains(id);
	}

	public void setMutationBranch(boolean mutationBranch) {
		this.mutationBranch = mutationBranch;
	}

	public void setMutatedBranch(boolean mutatedBranch) {
		this.mutatedBranch = mutatedBranch;
	}

	public void setBranchId(int branchId) {
		if (branchId < 0)
			throw new IllegalArgumentException(
					"expect branchId to be positive, not " + branchId);

		this.controlDependentBranchId = branchId;
	}

	public void setBranchExpressionValue(boolean branchExpressionValue) {
		this.branchExpressionValue = branchExpressionValue;
	}

	public boolean isMutatedBranch() {
		// Mutated if HOMObserver of MutationObserver are called
		return isBranch() && mutatedBranch;
	}

	public int getDistance(long id) {
		if (mutant_distance.containsKey(id))
			return mutant_distance.get(id);
		return Integer.MAX_VALUE;
	}

	public void setDistance(long id, int distance) {
		mutant_distance.put(id, distance);
	}

	public int getLineNumber() {
		// former method comment
		// If hasLineNumberSet() returns true, this method returns the
		// lineNumber of
		// this instruction Otherwise an IllegalStateException() will be thrown
		// to
		// indicate that the field was never initialized properly

		// if (!hasLineNumberSet()) // TODO if lineNumber not set retrieve this
		// info from ... CFGPool or something
		// throw new IllegalStateException(
		// "expect hasLineNumberSet() to be true on a BytecodeInstruction that gets asked for it's lineNumber");

		return lineNumber;
	}

	/**
	 *  
	 */
	public void setLineNumber(int lineNumber) {
		if (lineNumber <= 0)
			throw new IllegalArgumentException(
					"expect lineNumber value to be positive");

		if (isLineNumber()) {
			int asmLine = ((LineNumberNode) asmNode).line;
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
			int asmLine = ((LineNumberNode) asmNode).line;
			// sanity check
			if (this.lineNumber != -1 && asmLine != this.lineNumber)
				throw new IllegalStateException(
						"lineNumber field was manually set to a value different from the actual lineNumber contained in LineNumberNode");
			this.lineNumber = asmLine;
		}
	}

	// --- TODO CDG-Section WARNING: broken as hell TODO ---

	/**
	 * Returns the ControlDependenceGraph of this instructions method
	 * 
	 * Convenience method. Redirects the call to CFGPool.getCDG()
	 */
	public ControlDependenceGraph getCDG() {

		ControlDependenceGraph myCDG = CFGPool.getCDG(className,
				methodName);
		if (myCDG == null)
			throw new IllegalStateException(
					"expect CFGPool to know CDG for every method for which an instruction is known");
		
		return myCDG;
	}
	
	/**
	 * Returns a cfg.Branch object for each branch this instruction is control
	 * dependent on as determined by the ControlDependenceGraph. If this
	 * instruction is only dependent on the root branch this method returns an
	 * empty set
	 */
	public Set<Branch> getControlDependentBranches() {

		return getCDG().getControlDependentBranches(this);
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

		Set<Branch> cdIds = getControlDependentBranches();

		for (Branch cdId : cdIds)
			return cdId;

		return null;
	}

	/**
	 * Returns all branchIds of Branches this instruction is control dependent
	 * on as determined by the ControlDependenceGraph for this instruction's
	 * method.
	 */
	public Set<Integer> getControlDependentBranchIds() {

		ControlDependenceGraph myDependence = CFGPool.getCDG(className,
				methodName);
		if (myDependence == null)
			throw new IllegalStateException(
					"expect CFGPool to know CDG for every method for which an instruction is known");

		return myDependence.getControlDependentBranchIds(this);
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

		return getCDG().getBranchExpressionValue(this,b);
	}

	/**
	 * Determines whether the CFGVertex is transitively control dependent on the
	 * given Branch
	 * 
	 * A CFGVertex is transitively control dependent on a given Branch if the
	 * Branch and the vertex are in the same method and the vertex is either
	 * directly control dependent on the Branch - look at
	 * isDirectlyControlDependentOn(Branch) - or the CFGVertex of the control
	 * dependent branch of this CFGVertex is transitively control dependent on
	 * the given branch.
	 * 
	 */
	public boolean isTransitivelyControlDependentOn(Branch branch) {
		if (!getClassName().equals(branch.getClassName()))
			return false;
		if (!getMethodName().equals(branch.getMethodName()))
			return false;

		BytecodeInstruction vertexHolder = this;
		do {
			if (vertexHolder.isDirectlyControlDependentOn(branch))
				return true;
			vertexHolder = vertexHolder.getControlDependentBranch();
		} while (vertexHolder != null);

		return false;
	}

	/**
	 * Determines whether this BytecodeInstruction is directly control dependent
	 * on the given Branch. Meaning within this instruction CDG there is an
	 * incoming ControlFlowEdge to this instructions BasicBlock holding the
	 * given Branch as it's branchInstruction
	 */
	public boolean isDirectlyControlDependentOn(Branch branch) {
		return getCDG().isDirectlyControlDependentOn(this, branch);
	}

//	 /**
//	 * Supposed to return the Branch this CFGVertex is control dependent on
//	 null
//	 * if it's only dependent on the root branch
//	 */
//	 public Branch getControlDependentBranch() {
//	 // TODO fix this, broken!
//	 // TODO fails if this.v is a branch UPDATE just fails so hard
//	 // TODO this is the big goal right now!
//	 // quick fix idea: take byteCode instruction directly
//	 // previous to the branch (id-1)
//	 // this is should have correct branchId and branchExpressionValue
//	 if (isActualBranch()) {
//	 BytecodeInstruction hope = CFGPool.getRawCFG(getClassName(),
//	 getMethodName()).getInstruction(getVertexId() - 1);
//	 if (hope == null)
//	 return null;
//	 return hope.getControlDependentBranch();
//	 }
//	 return BranchPool.getBranch(getControlDependentBranchId());
//	 }

	/**
	 * Determines the number of branches that have to be passed in order to pass
	 * this CFGVertex
	 * 
	 * Used to determine TestFitness difficulty
	 */
	public int getCDGDepth() {
		// TODO fix this, broken
		Branch current = getControlDependentBranch();
		int r = 1;
		while (current != null) {
			r++;
			current = current.getControlDependentBranch();
		}
		return r;
	}

	/*
	 * public void addControlDependentBranch(Branch branch) {
	 * controlDependencies.add(branch); }
	 * 
	 * public Set<Branch> getControlDependencies() { return controlDependencies;
	 * }
	 */

	// --- Inherited from Object ---

	@Override
	public String toString() {

		if (isMutation()) {
			String ids = "Mutations: ";
			for (long l : mutations) {
				ids += " " + l;
			}
			return ids;
		}
		if (isActualBranch()) {
			if (BranchPool.isKnownAsBranch(this)) {
				Branch b = BranchPool.getBranchForInstruction(this);
				if (b == null)
					throw new IllegalStateException(
							"expect BranchPool to be able to return Branches for instructions fullfilling BranchPool.isKnownAsBranch()");

				return "Branch " + b.getActualBranchId() + " - "
						+ getInstructionType();
			}
			return "UNKNOWN Branch i" + instructionId + " "
					+ getInstructionType();

			// + " - " + ((JumpInsnNode) asmNode).label.getLabel();
		}
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
			return "Field" + " " + asmNode.getOpcode() + " Type=" + type
					+ ", Opcode=" + opcode;
		else if (asmNode instanceof FrameNode)
			return "Frame" + " " + asmNode.getOpcode() + " Type=" + type
					+ ", Opcode=" + opcode;
		else if (asmNode instanceof IincInsnNode)
			return "IINC " + ((IincInsnNode) asmNode).var + " Type=" + type
					+ ", Opcode=" + opcode;
		else if (asmNode instanceof InsnNode)
			return "" + opcode;
		else if (asmNode instanceof IntInsnNode)
			return "INT " + ((IntInsnNode) asmNode).operand + " Type=" + type
					+ ", Opcode=" + opcode;
		else if (asmNode instanceof MethodInsnNode)
			return opcode + " " + ((MethodInsnNode) asmNode).name;
		else if (asmNode instanceof JumpInsnNode)
			return "JUMP " + ((JumpInsnNode) asmNode).label.getLabel()
					+ " Type=" + type + ", Opcode=" + opcode + ", Stack: "
					+ stack + " - Line: " + lineNumber;
		else if (asmNode instanceof LdcInsnNode)
			return "LDC " + ((LdcInsnNode) asmNode).cst + " Type=" + type
					+ ", Opcode=" + opcode; // cst starts with mutationid if
		// this is location of mutation
		else if (asmNode instanceof LineNumberNode)
			return "LINE " + " " + ((LineNumberNode) asmNode).line;
		else if (asmNode instanceof LookupSwitchInsnNode)
			return "LookupSwitchInsnNode" + " " + asmNode.getOpcode()
					+ " Type=" + type + ", Opcode=" + opcode;
		else if (asmNode instanceof MultiANewArrayInsnNode)
			return "MULTIANEWARRAY " + " " + asmNode.getOpcode() + " Type="
					+ type + ", Opcode=" + opcode;
		else if (asmNode instanceof TableSwitchInsnNode)
			return "TableSwitchInsnNode" + " " + asmNode.getOpcode() + " Type="
					+ type + ", Opcode=" + opcode;
		else if (asmNode instanceof TypeInsnNode)
			return "NEW " + ((TypeInsnNode) asmNode).desc;
		// return "TYPE " + " " + node.getOpcode() + " Type=" + type
		// + ", Opcode=" + opcode;
		else if (asmNode instanceof VarInsnNode)
			return opcode + " " + ((VarInsnNode) asmNode).var;
		else
			return "Unknown node" + " Type=" + type + ", Opcode=" + opcode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof BytecodeInstruction))
			return false;

		// TODO ensure that the following checks always succeed
		// TODO do this by ensuring that those values are always set correctly

		BytecodeInstruction other = (BytecodeInstruction) obj;

		if (instructionId != other.instructionId)
			return false;
		if (methodName != null && !methodName.equals(other.methodName))
			return false;
		if (className != null && !className.equals(other.className))
			return false;

		return super.equals(obj);
	}

}