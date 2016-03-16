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
package org.evosuite.coverage.lcsaj;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.Properties.Strategy;
import org.evosuite.coverage.branch.Branch;
import org.evosuite.coverage.branch.BranchPool;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LCSAJ implements Comparable<LCSAJ> {

	private static Logger logger = LoggerFactory.getLogger(LCSAJ.class);

	// All branches passed in the LCSAJ
	private final List<Branch> branches = new ArrayList<Branch>();
	// Information needed and maintained in the LCSAJ instrumentation and detection algorithm
	private AbstractInsnNode lastAccessedNode;

	private int id = -1;
	// Class and method where the LCSAJ occurs
	private final String className;
	private final String methodName;

	private int positionReached = 0;

	/**
	 * <p>
	 * Constructor for LCSAJ.
	 * </p>
	 * 
	 * @param className
	 *            a {@link java.lang.String} object.
	 * @param methodName
	 *            a {@link java.lang.String} object.
	 * @param start
	 *            a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
	 */
	public LCSAJ(String className, String methodName, BytecodeInstruction start) {
		this.className = className;
		this.methodName = methodName;
		this.lastAccessedNode = start.getASMNode();

		if (!BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).isKnownAsBranch(start)) {
			if (methodName.startsWith("<init>") && start.getInstructionId() <= 1) {

			}
			if (Properties.STRATEGY != Strategy.EVOSUITE) {
				start.forceBranch();
				BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).registerAsBranch(start);
				logger.info("Registering new branch for start node");
			}
		}

		Branch branch = BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getBranchForInstruction(start);
		branches.add(branch);
	}

	//Copy constructor
	/**
	 * <p>
	 * Constructor for LCSAJ.
	 * </p>
	 * 
	 * @param l
	 *            a {@link org.evosuite.coverage.lcsaj.LCSAJ} object.
	 */
	public LCSAJ(LCSAJ l) {
		this.className = l.getClassName();
		this.methodName = l.getMethodName();

		this.branches.addAll(l.branches);

		this.lastAccessedNode = l.getLastNodeAccessed();
	}

	/**
	 * <p>
	 * getID
	 * </p>
	 * 
	 * @return a int.
	 */
	public int getID() {
		return this.id;
	}

	/**
	 * Set an (not necessarily unique) new ID for a LCSAJ. While adding a LCSAJ
	 * into the Pool the number is set to its occurrence during the detection of
	 * all LCSAJ in a method
	 * 
	 * @param id
	 *            a int.
	 */
	public void setID(int id) {
		this.id = id;
	}

	/**
	 * <p>
	 * getBranchInstructions
	 * </p>
	 * 
	 * @return a {@link java.util.List} object.
	 */
	public List<Branch> getBranchInstructions() {
		return branches;
	}

	/**
	 * <p>
	 * getBranch
	 * </p>
	 * 
	 * @param position
	 *            a int.
	 * @return a {@link org.evosuite.coverage.branch.Branch} object.
	 */
	public Branch getBranch(int position) {
		return branches.get(position);
	}

	/**
	 * <p>
	 * getBranchID
	 * </p>
	 * 
	 * @param position
	 *            a int.
	 * @return a int.
	 */
	public int getBranchID(int position) {
		return branches.get(position).getActualBranchId();
	}

	/**
	 * <p>
	 * getStartBranch
	 * </p>
	 * 
	 * @return a {@link org.evosuite.coverage.branch.Branch} object.
	 */
	public Branch getStartBranch() {
		return branches.get(0);
	}

	/**
	 * <p>
	 * getLastBranch
	 * </p>
	 * 
	 * @return a {@link org.evosuite.coverage.branch.Branch} object.
	 */
	public Branch getLastBranch() {
		return branches.get(branches.size() - 1);
	}

	/**
	 * <p>
	 * getLastNodeAccessed
	 * </p>
	 * 
	 * @return a {@link org.objectweb.asm.tree.AbstractInsnNode} object.
	 */
	public AbstractInsnNode getLastNodeAccessed() {
		return lastAccessedNode;
	}

	/**
	 * <p>
	 * Getter for the field <code>className</code>.
	 * </p>
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String getClassName() {
		return this.className;
	}

	/**
	 * <p>
	 * Getter for the field <code>methodName</code>.
	 * </p>
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String getMethodName() {
		return this.methodName;
	}

	/**
	 * <p>
	 * lookupInstruction
	 * </p>
	 * 
	 * @param id
	 *            a int.
	 * @param instruction
	 *            a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
	 */
	public void lookupInstruction(int id, BytecodeInstruction instruction) {
		lastAccessedNode = instruction.getASMNode();

		if (instruction.isBranch()) {
			Branch branch = BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getBranchForInstruction(instruction);
			branches.add(branch);

		} else if (instruction.isReturn() || instruction.isThrow()
		        || instruction.isGoto()) {

			if (Properties.STRATEGY != Strategy.EVOSUITE
			        && !BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).isKnownAsBranch(instruction)) {
				instruction.forceBranch();
				BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).registerAsBranch(instruction);
				logger.info("Registering new branch");
			}

			Branch branch = BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getBranchForInstruction(instruction);
			branches.add(branch);
		}
	}

	/**
	 * <p>
	 * length
	 * </p>
	 * 
	 * @return a int.
	 */
	public int length() {
		return branches.size();
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		String output = "LCSAJ no.: " + this.id;
		output += " in " + this.className + "/" + this.methodName + ". Branches passed: ";
		for (Branch b : branches)
			output += " -> " + b.getActualBranchId();
		//output += "\n";
		// for (Branch b : branches)
		//	output += " -> " + b.getASMNodeString() + "\n";
		return output;
	}

	/**
	 * <p>
	 * Setter for the field <code>positionReached</code>.
	 * </p>
	 * 
	 * @param position
	 *            a int.
	 */
	public void setPositionReached(int position) {
		this.positionReached = position;
	}

	/**
	 * <p>
	 * getdPositionReached
	 * </p>
	 * 
	 * @return a int.
	 */
	public int getdPositionReached() {
		return this.positionReached;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(LCSAJ o) {
		for (int i = 0; i < Math.min(branches.size(), o.length()); i++) {
			if (branches.get(i).compareTo(o.getBranch(i)) != 0)
				return branches.get(i).compareTo(o.getBranch(i));
		}
		return length() - o.length();
	}
}
