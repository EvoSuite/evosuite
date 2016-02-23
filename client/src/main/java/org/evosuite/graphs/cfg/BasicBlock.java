/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.graphs.cfg;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.evosuite.graphs.GraphPool;
import org.evosuite.graphs.cdg.ControlDependenceGraph;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to represent basic blocks in the control flow graph.
 * 
 * A basic block is a list of instructions for which the following holds:
 * 
 * Whenever control flow reaches the first instruction of this blocks list,
 * control flow will pass through all the instructions of this list successively
 * and not pass another instruction of the underlying method in the mean time.
 * The first element in this blocks list does not have a parent in the CFG that
 * can be prepended to the list and the same would still hold true Finally the
 * last element in this list does not have a child inside the CFG that could be
 * appended to the list such that the above still holds true
 * 
 * In other words: - the first/last element of this blocks list has either 0 or
 * >=2 parents/children in the CFG - every other element in the list has exactly
 * 1 parent and exactly 1 child in the raw CFG
 * 
 * 
 * Taken from:
 * 
 * "Efficiently Computing Static Single Assignment Form and the Control
 * Dependence Graph" RON CYTRON, JEANNE FERRANTE, BARRY K. ROSEN, and MARK N.
 * WEGMAN IBM Research Division and F. KENNETH ZADECK Brown University 1991
 * 
 * @see cfg.ActualControlFlowGraph
 * @author Andre Mis
 */
public class BasicBlock implements Serializable, Iterable<BytecodeInstruction> {

	private static final long serialVersionUID = -3465486470017841484L;

	private static Logger logger = LoggerFactory.getLogger(BasicBlock.class);

	private static int blockCount = 0;

	private int id = -1;
	protected ClassLoader classLoader;
	protected String className;
	protected String methodName;

	// experiment: since finding the control dependent branches in the CDG might
	// take a little to long, we might want to remember them
	private Set<ControlDependency> controlDependencies;
	private Set<Integer> controlDependentBranchIDs;

	protected boolean isAuxiliaryBlock = false;

	private final List<BytecodeInstruction> instructions = new ArrayList<BytecodeInstruction>();

	// DONE reference each BytecodeInstruction's BasicBlock at the instruction
	// DONE determine ControlDependentBranches once for each BasicBlock, then
	// ask BasicBloc, whenever instruction is asked
	// TODO remember distance to each control dependent Branch in order to speed
	// up ControlFlowDistance calculation even more

	/**
	 * <p>
	 * Constructor for BasicBlock.
	 * </p>
	 * 
	 * @param className
	 *            a {@link java.lang.String} object.
	 * @param methodName
	 *            a {@link java.lang.String} object.
	 * @param blockNodes
	 *            a {@link java.util.List} object.
	 */
	public BasicBlock(ClassLoader classLoader, String className, String methodName,
	        List<BytecodeInstruction> blockNodes) {
		if (className == null || methodName == null || blockNodes == null)
			throw new IllegalArgumentException("null given");

		this.className = className;
		this.methodName = methodName;
		this.classLoader = classLoader;

		setId();
		setInstructions(blockNodes);

		checkSanity();
	}

	/**
	 * Used by Entry- and ExitBlocks
	 * 
	 * @param className
	 *            a {@link java.lang.String} object.
	 * @param methodName
	 *            a {@link java.lang.String} object.
	 */
	protected BasicBlock(String className, String methodName) {
		if (className == null || methodName == null)
			throw new IllegalArgumentException("null given");

		this.className = className;
		this.methodName = methodName;
		this.isAuxiliaryBlock = true;
	}

	// CDs

	/**
	 * Returns the ControlDependenceGraph of this instructions method
	 * 
	 * Convenience method. Redirects the call to GraphPool.getCDG()
	 * 
	 * @return a {@link org.evosuite.graphs.cdg.ControlDependenceGraph} object.
	 */
	public ControlDependenceGraph getCDG() {

		ControlDependenceGraph myCDG = GraphPool.getInstance(classLoader).getCDG(className,
		                                                                         methodName);
		if (myCDG == null)
			throw new IllegalStateException(
			        "expect GraphPool to know CDG for every method for which an instruction is known");

		return myCDG;
	}

	/**
	 * Returns all branchIds of Branches this instruction is directly control
	 * dependent on as determined by the ControlDependenceGraph for this
	 * instruction's method.
	 * 
	 * If this instruction is control dependent on the root branch the id -1
	 * will be contained in this set
	 * 
	 * @return a {@link java.util.Set} object.
	 */
	public Set<Integer> getControlDependentBranchIds() {

		ControlDependenceGraph myDependence = getCDG();

		if (controlDependentBranchIDs == null) {
			controlDependentBranchIDs = myDependence.getControlDependentBranchIds(this);
			//be sure we can iterate over it deterministically
			controlDependentBranchIDs =
					controlDependentBranchIDs.stream().sorted().collect(Collectors.toCollection(LinkedHashSet::new));
		}
		return controlDependentBranchIDs;
	}

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
	 * 
	 * @return a {@link java.util.Set} object.
	 */
	public Set<ControlDependency> getControlDependencies() {

		if (controlDependencies == null)
			controlDependencies = getCDG().getControlDependentBranches(this);

		//		return new HashSet<ControlDependency>(controlDependentBranches);
		return controlDependencies;
	}

	/**
	 * <p>
	 * hasControlDependenciesSet
	 * </p>
	 * 
	 * @return a boolean.
	 */
	public boolean hasControlDependenciesSet() {
		return controlDependencies != null;
	}

	// initialization

	private void setInstructions(List<BytecodeInstruction> blockNodes) {
		for (BytecodeInstruction instruction : blockNodes) {
			if (!appendInstruction(instruction))
				throw new IllegalStateException(
				        "internal error while addind instruction to basic block list");
		}
		if (instructions.isEmpty())
			throw new IllegalStateException(
			        "expect each basic block to contain at least one instruction");
	}

	private boolean appendInstruction(BytecodeInstruction instruction) {
		if (instruction == null)
			throw new IllegalArgumentException("null given");
		if (!className.equals(instruction.getClassName()))
			throw new IllegalArgumentException(
			        "expect elements of a basic block to be inside the same class");
		if (!methodName.equals(instruction.getMethodName()))
			throw new IllegalArgumentException(
			        "expect elements of a basic block to be inside the same class");
		if (instruction.hasBasicBlockSet())
			throw new IllegalArgumentException(
			        "expect to get instruction without BasicBlock already set");
		if (instructions.contains(instruction))
			throw new IllegalArgumentException(
			        "a basic block can not contain the same element twice");

		// not sure if this holds:
		// .. apparently it doesn't. at least check
		// fails for java2.util2.Pattern TODO

		// BytecodeInstruction previousInstruction = getLastInstruction();
		// if (previousInstruction != null
		// && instruction.getInstructionId() < previousInstruction
		// .getInstructionId())
		// throw new IllegalStateException(
		// "expect instructions in a basic block to be ordered by their instructionId");

		instruction.setBasicBlock(this);

		return instructions.add(instruction);
	}

	private void setId() {
		blockCount++;
		this.id = blockCount;
	}

	// retrieve information

	/**
	 * <p>
	 * containsInstruction
	 * </p>
	 * 
	 * @param instruction
	 *            a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
	 * @return a boolean.
	 */
	public boolean containsInstruction(BytecodeInstruction instruction) {
		if (instruction == null)
			throw new IllegalArgumentException("null given");

		return instructions.contains(instruction);
	}

	/**
	 * <p>
	 * constainsInstruction
	 * </p>
	 * 
	 * @param insnNode
	 *            a {@link org.objectweb.asm.tree.AbstractInsnNode} object.
	 * @return a boolean.
	 */
	public boolean constainsInstruction(AbstractInsnNode insnNode) {
		for (BytecodeInstruction instruction : instructions) {
			if (instruction.getASMNode().equals(insnNode))
				return true;
		}
		return false;
	}

	/**
	 * <p>
	 * getFirstInstruction
	 * </p>
	 * 
	 * @return a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
	 */
	public BytecodeInstruction getFirstInstruction() {
		if (instructions.isEmpty())
			return null;
		return instructions.get(0);
	}

	/**
	 * <p>
	 * getLastInstruction
	 * </p>
	 * 
	 * @return a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
	 */
	public BytecodeInstruction getLastInstruction() {
		if (instructions.isEmpty())
			return null;
		return instructions.get(instructions.size() - 1);
	}

	/**
	 * <p>
	 * getFirstLine
	 * </p>
	 * 
	 * @return a int.
	 */
	public int getFirstLine() {
		for (BytecodeInstruction ins : instructions)
			if (ins.hasLineNumberSet())
				return ins.getLineNumber();

		return -1;
	}

	/**
	 * <p>
	 * getLastLine
	 * </p>
	 * 
	 * @return a int.
	 */
	public int getLastLine() {

		int r = -1;

		for (BytecodeInstruction ins : instructions)
			if (ins.hasLineNumberSet())
				r = ins.getLineNumber();

		return r;
	}

	/**
	 * <p>
	 * getName
	 * </p>
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String getName() {
		return (isAuxiliaryBlock ? "aux" : "") + "BasicBlock " + id;
		// +" - "+methodName;
	}

	/**
	 * <p>
	 * Getter for the field <code>className</code>.
	 * </p>
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * <p>
	 * Getter for the field <code>methodName</code>.
	 * </p>
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String getMethodName() {
		return methodName;
	}

	/**
	 * <p>
	 * explain
	 * </p>
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String explain() {
		StringBuilder r = new StringBuilder();
		r.append(getName() + ":\n");

		int i = 0;
		for (BytecodeInstruction instruction : instructions) {
			i++;
			r.append("\t" + i + ")\t" + instruction.toString() + "\n");
		}

		return r.toString();
	}

	// inherited from Object

	/** {@inheritDoc} */
	@Override
	public String toString() {

		String r = "BB" + id;

		if (instructions.size() < 5)
			for (BytecodeInstruction ins : instructions)
				r = r.trim() + " " + ins.getInstructionType();
		else
			r += " " + getFirstInstruction().getInstructionType() + " ... "
			        + getLastInstruction().getInstructionType();

		int startLine = getFirstLine();
		int endLine = getLastLine();
		r += " l" + (startLine == -1 ? "?" : startLine + "");
		r += "-l" + (endLine == -1 ? "?" : endLine + "");

		return r;
	}



	// sanity check

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((className == null) ? 0 : className.hashCode());
		result = prime * result + id;
		result = prime * result
				+ ((instructions == null) ? 0 : instructions.hashCode());
		result = prime * result + (isAuxiliaryBlock ? 1231 : 1237);
		result = prime * result
				+ ((methodName == null) ? 0 : methodName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof BasicBlock))
			return false;
		BasicBlock other = (BasicBlock) obj;
		if (id != other.id)
			return false;
		if (className == null) {
			if (other.className != null)
				return false;
		} else if (!className.equals(other.className))
			return false;
		if (methodName == null) {
			if (other.methodName != null)
				return false;
		} else if (!methodName.equals(other.methodName))
			return false;
		if (instructions == null) {
			if (other.instructions != null)
				return false;
		} else if (!instructions.equals(other.instructions))
			return false;
		if (isEntryBlock() != other.isEntryBlock())
			return false;
		if (isExitBlock() != other.isExitBlock())
			return false;
		return true;
	}

	/**
	 * <p>
	 * checkSanity
	 * </p>
	 */
	public void checkSanity() {

		logger.debug("checking sanity of " + toString());

		// TODO

		// not true, there are branches that don't really jump
		// for example if you have no statement in a then-part:
		// if (exp) { ; }
		// you will not have a second outgoing edge for that if

		// for(BytecodeInstruction instruction : instructions) {
		// if (!instruction.equals(getLastInstruction())
		// && instruction.isActualBranch())
		// throw new IllegalStateException(
		// "expect actual branches to always end a basic block: "+instruction.toString()+" \n"+explain());
		// }

		// TODO handle specialBlocks
	}

	/**
	 * <p>
	 * isEntryBlock
	 * </p>
	 * 
	 * @return a boolean.
	 */
	public boolean isEntryBlock() {
		return false;
	}

	/**
	 * <p>
	 * isExitBlock
	 * </p>
	 * 
	 * @return a boolean.
	 */
	public boolean isExitBlock() {
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	/** {@inheritDoc} */
	@Override
	public Iterator<BytecodeInstruction> iterator() {
		return instructions.iterator();
	}
}
