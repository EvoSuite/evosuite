package de.unisb.cs.st.evosuite.cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.objectweb.asm.util.AbstractVisitor;

import de.unisb.cs.st.evosuite.coverage.branch.Branch;
import de.unisb.cs.st.evosuite.coverage.branch.BranchPool;

/**
 * Internal representation of a BytecodeInstruction
 * Wraps asm.tree.AbstractInsnNode and is used in
 * cfg.BasicBlock and transitively cfg.ControlFlowGraph
 *  
 * 
 * Old: Node of the control flow graph
 * 
 * @author Gordon Fraser, Andre Mis
 * 
 */
public class BytecodeInstruction extends ASMWrapper {

	//	---						 - Fields - 							---

	/**
	 * Can represent any byteCode instruction  
	 */
	public BytecodeInstruction (String className, String methodName,
			int instructionId, AbstractInsnNode node, int lineNumber) {
		this(className, methodName, instructionId, node);
		this.lineNumber = lineNumber;
	}
	
	public BytecodeInstruction (String className, String methodName,
			int instructionId, AbstractInsnNode node) {
		this.instructionId = instructionId;
		this.node = node;
		this.methodName = methodName;
		this.className = className;
	}

	/**
	 * "copy-Constructor"
	 */
	public BytecodeInstruction (BytecodeInstruction wrap) {
		this(wrap.className, wrap.methodName, wrap.instructionId, wrap.node,
				wrap.lineNumber);
	}


	//			 ---	   		 - General -				---
	int globalBytecodeInstructionId;
	
	//			 ---			- Mutations	-				---
	// Calculate distance to each mutation
	Map<Long, Integer> mutant_distance = new HashMap<Long, Integer>(); 
	private final List<Long> mutations = new ArrayList<Long>();
	boolean mutationBranch = false;
	boolean mutatedBranch = false;
	
	//			 --			- Coverage Criteria -			---
	public int branchId = -1;
	// TODO branchExpressionValue should be false whenever it is true and visa versa
	public boolean branchExpressionValue = true;
	
	// TODO: every BytecodeInstruction should 
	// ..hold a reference to it's control dependent Branch
	// ..actually a CFGVertex should hold a 
	// ..set of all branches it is control dependent on:
	// private Set<Branch> controlDependencies = new HashSet<Branch>();
	
	
	// ---					- Constructors - 						---

	
//	public BytecodeInstruction(int instructionId, AbstractInsnNode node) {
//		this.instructionId = instructionId;
//		this.node = node;
//	}

//	/**
//	 * Sort of a copy constructor ... TODO is this ugly?
//	 * 
//	 */
//	public BytecodeInstruction(BytecodeInstruction clone) {
//		this(clone.instructionId, clone.node);
//	}
	


	//		---				TODO CDG-Section WARNING: broken as hell TODO		---
	

	/**
	 * Determines whether the CFGVertex is transitively control dependent
	 * on the given Branch
	 * 
	 * A CFGVertex is transitively control dependent on a given Branch
	 * if the Branch and the vertex are in the same method and the
	 * vertex is either directly control dependent on the Branch
	 * - look at isDirectlyControlDependentOn(Branch) -
	 * or the CFGVertex of the control dependent branch of this CFGVertex
	 * is transitively control dependent on the given branch. 
	 * 
	 */
	public boolean isTransitivelyControlDependentOn(Branch branch) {
		if(!getClassName().equals(branch.getClassName()))
			return false;		
		if(!getMethodName().equals(branch.getMethodName()))
			return false;
		
		BytecodeInstruction vertexHolder = this;
		do {
			if(vertexHolder.isControlDependentOn(branch))
				return true;
			vertexHolder = vertexHolder.getControlDependentBranch();
		} while (vertexHolder != null);
		
		return false;
	}
	
	/**
	 * Determines whether this CFGVertex is directly control dependent on the given Branch
	 *  meaning they share the same branchId and branchExpressionValue
	 */
	public boolean isControlDependentOn(Branch branch) {
		if(!getClassName().equals(branch.getClassName()))
			return false;		
		if(!getMethodName().equals(branch.getMethodName()))
			return false;
		
		return branch.getBranchId()==getBranchId() 
		&& branch.getBranchExpressionValue()==getBranchExpressionValue();		
	}
	
	/**
	 * Supposed to return the Branch this CFGVertex is control dependent on
	 * null if it's only dependent on the root branch 
	 */
	public Branch getControlDependentBranch() {
		// TODO fix this, broken!
		// TODO fails if this.v is a branch UPDATE just fails so hard
		// TODO this is the big goal right now!
		// quick fix idea: take byteCode instruction directly
		//		previous to the branch (id-1)
		//		this is should have correct branchId and branchExpressionValue
		if(isActualBranch()) {
			BytecodeInstruction hope = 
				CFGMethodAdapter.getCompleteCFG(getClassName(), getMethodName()).getVertex(getVertexId()-1);
			if(hope==null)
				return null;
			return hope.getControlDependentBranch();
		}
		return BranchPool.getBranch(getBranchId());
	}
	
	/**
	 * Determines the number of branches that have to be passed in order to
	 * pass this CFGVertex
	 * 
	 * Used to determine TestFitness difficulty
	 */
	public int getCDGDepth() {
		// TODO fix this, broken
		Branch current = getControlDependentBranch();
		int r = 1;
		while(current!=null) {
			r++;
			current = current.getControlDependentBranch();
		}
		return r;
	}
	
	/*
	public void addControlDependentBranch(Branch branch) {
		controlDependencies.add(branch);
	}
			
	public Set<Branch> getControlDependencies() {
		return controlDependencies;
	}
	*/
	

	// ---	Getters and Setters - TODO find out which ones to hide/remove	---

	
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
	public boolean getBranchExpressionValue() {
		return branchExpressionValue;
	}
	
	public int getBranchId() {
		return branchId;
	}

	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber; 
	}
	
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

	public Map<Long, Integer> getMutant_distance() {
		return mutant_distance;
	}

	public void setMutant_distance(Map<Long, Integer> mutantDistance) {
		mutant_distance = mutantDistance;
	}

	public List<Long> getMutations() {
		return mutations;
	}

	public void setMutationBranch(boolean mutationBranch) {
		this.mutationBranch = mutationBranch;
	}

	public void setMutatedBranch(boolean mutatedBranch) {
		this.mutatedBranch = mutatedBranch;
	}

	public void setBranchId(int branchId) {
		this.branchId = branchId;
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
	
	
	//	---				Inherited from Object				---
	
	
	@Override
	public String toString() {

		if (isMutation()) {
			String ids = "Mutations: ";
			for (long l : mutations) {
				ids += " " + l;
			}
			return ids;
		}
		if (isBranch()) {
			return "Branch " + branchId + " - " + ((JumpInsnNode) node).label.getLabel();
		}
		String type = "";
		String opcode = "";
		if (node.getOpcode() >= 0
				&& node.getOpcode() < AbstractVisitor.OPCODES.length)
			opcode = AbstractVisitor.OPCODES[node.getOpcode()];
		if (node.getType() >= 0 && node.getType() < AbstractVisitor.TYPES.length)
			type = AbstractVisitor.TYPES[node.getType()];
		String stack = "";
		if (frame == null)
			stack = "null";
		else
			for (int i = 0; i < frame.getStackSize(); i++) {
				stack += frame.getStack(i) + ",";
			}
		
		if (node instanceof LabelNode) {
			return "LABEL " + ((LabelNode) node).getLabel().toString();
		} else if (node instanceof FieldInsnNode)
			return "Field" + " " + node.getOpcode() + " Type=" + type
					+ ", Opcode=" + opcode;
		else if (node instanceof FrameNode)
			return "Frame" + " " + node.getOpcode() + " Type=" + type
					+ ", Opcode=" + opcode;
		else if (node instanceof IincInsnNode)
			return "IINC " + ((IincInsnNode) node).var + " Type=" + type
					+ ", Opcode=" + opcode;
		else if (node instanceof InsnNode)
			return "" + opcode;
		else if (node instanceof IntInsnNode)
			return "INT " + ((IntInsnNode) node).operand + " Type=" + type
					+ ", Opcode=" + opcode;
		else if (node instanceof MethodInsnNode)
			return opcode + " " + ((MethodInsnNode) node).name;
		else if (node instanceof JumpInsnNode)
			return "JUMP " + ((JumpInsnNode) node).label.getLabel() + " Type="
					+ type + ", Opcode=" + opcode + ", Stack: " + stack
					+ " - Line: " + lineNumber;
		else if (node instanceof LdcInsnNode)
			return "LDC " + ((LdcInsnNode) node).cst + " Type=" + type
					+ ", Opcode=" + opcode; // cst starts with mutationid if
		// this is location of mutation
		else if (node instanceof LineNumberNode)
			return "LINE " + " " + ((LineNumberNode) node).line;
		else if (node instanceof LookupSwitchInsnNode)
			return "LookupSwitchInsnNode" + " " + node.getOpcode() + " Type="
					+ type + ", Opcode=" + opcode;
		else if (node instanceof MultiANewArrayInsnNode)
			return "MULTIANEWARRAY " + " " + node.getOpcode() + " Type=" + type
					+ ", Opcode=" + opcode;
		else if (node instanceof TableSwitchInsnNode)
			return "TableSwitchInsnNode" + " " + node.getOpcode() + " Type="
					+ type + ", Opcode=" + opcode;
		else if (node instanceof TypeInsnNode)
			return "NEW " + ((TypeInsnNode) node).desc;
		// return "TYPE " + " " + node.getOpcode() + " Type=" + type
		// + ", Opcode=" + opcode;
		else if (node instanceof VarInsnNode)
			return opcode + " " + ((VarInsnNode) node).var;
		else
			return "Unknown node" + " Type=" + type + ", Opcode=" + opcode;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass()) // TODO: can Class be compared via == ?
			return false;
		BytecodeInstruction other = (BytecodeInstruction) obj;
		// if (!getOuterType().equals(other.getOuterType()))
		// return false;
		if (instructionId != other.instructionId)
			return false;
		if (methodName != null && !methodName.equals(other.methodName))
			return false;
		if (className != null && !className.equals(other.className))
			return false;
		return true;
	}
}