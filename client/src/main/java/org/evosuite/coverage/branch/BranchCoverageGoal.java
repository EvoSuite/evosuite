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
package org.evosuite.coverage.branch;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.ControlFlowDistance;
import org.evosuite.graphs.cfg.BytecodeInstructionPool;
import org.evosuite.graphs.cfg.ControlDependency;
import org.evosuite.testcase.execution.ExecutionResult;

/**
 * A single branch coverage goal Either true/false evaluation of a jump
 * condition, or a method entry
 * 
 * @author Gordon Fraser, Andre Mis
 */
public class BranchCoverageGoal implements Serializable, Comparable<BranchCoverageGoal> {

	private static final long serialVersionUID = 2962922303111452419L;
	
	private transient Branch branch;
	
	private final boolean value;
	private final String className;
	private final String methodName;
	
	
	/**
	 * The line number in the source code. This information is stored in the bytecode if the
	 * code was compiled in debug mode. If no info, we would get a negative value (e.g., -1) here.
	 */
	private final int lineNumber;

	public int getId() {
		return branch.getActualBranchId();

	}
	
	/**
	 * Can be used to create an arbitrary {@code BranchCoverageGoal} trying to cover the
	 * given {@code Branch}
	 * 
	 * <p>
	 * If the given branch is {@code null}, this goal will try to cover the root branch
	 * of the method identified by the given name - meaning it will just try to
	 * call the method at hand
	 * 
	 * <p>
	 * Otherwise this goal will try to reach the given branch and if value is
	 * true, make the branchInstruction jump and visa versa
	 * 
	 * @param branch
	 *            a {@link org.evosuite.coverage.branch.Branch} object.
	 * @param value
	 *            a boolean.
	 * @param className
	 *            a {@link java.lang.String} object.
	 * @param methodName
	 *            a {@link java.lang.String} object.
	 */
	public BranchCoverageGoal(Branch branch, boolean value, String className,
	        String methodName) {
		if (className == null || methodName == null)
			throw new IllegalArgumentException("null given");
		if (branch == null && !value)
			throw new IllegalArgumentException(
			        "expect goals for a root branch to always have value set to true");

		this.branch = branch;
		this.value = value;

		this.className = className;
		this.methodName = methodName;

		if (branch != null) {
			lineNumber = branch.getInstruction().getLineNumber();
			if (!branch.getMethodName().equals(methodName)
			        || !branch.getClassName().equals(className))
				throw new IllegalArgumentException(
				        "expect explicitly given information about a branch to coincide with the information given by that branch");
		} else {
			lineNumber = BytecodeInstructionPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT())
					.getFirstLineNumberOfMethod(className,methodName);
		}
	}

	/**
	 * Constructor accepting line number as parameter.
	 * @param branch
	 *            a {@link org.evosuite.coverage.branch.Branch} object.
	 * @param value
	 *            a boolean.
	 * @param className
	 *            a {@link java.lang.String} object.
	 * @param methodName
	 *            a {@link java.lang.String} object.
	 * @param lineNumber
	 *            an integer.
	 */
	public BranchCoverageGoal(Branch branch, boolean value, String className,
	                          String methodName, int lineNumber) {

		if (className == null || methodName == null)
			throw new IllegalArgumentException("null given");

		if (branch == null && !value)
			throw new IllegalArgumentException("expect goals for a root branch to always have value set to true");

		this.branch = branch;
		this.value = value;
		this.className = className;
		this.methodName = methodName;
		this.lineNumber = lineNumber;
	}

	/**
	 * <p>
	 * Constructor for BranchCoverageGoal.
	 * </p>
	 * 
	 * @param cd
	 *            a {@link org.evosuite.graphs.cfg.ControlDependency} object.
	 * @param className
	 *            a {@link java.lang.String} object.
	 * @param methodName
	 *            a {@link java.lang.String} object.
	 */
	public BranchCoverageGoal(ControlDependency cd, String className, String methodName) {
		this(cd.getBranch(), cd.getBranchExpressionValue(), className, methodName);
	}

	/**
	 * Methods that have no branches don't need a cfg, so we just set the cfg to
	 * null
	 * 
	 * @param className
	 *            a {@link java.lang.String} object.
	 * @param methodName
	 *            a {@link java.lang.String} object.
	 */
	public BranchCoverageGoal(String className, String methodName) {
		this.branch = null;
		this.value = true;

		this.className = className;
		this.methodName = methodName;
		lineNumber = BytecodeInstructionPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT())
				.getFirstLineNumberOfMethod(className,  methodName);		                                                                                                                  
	}

	/**
	 * @return the branch
	 */
	public Branch getBranch() {
		return branch;
	}

	/**
	 * @return the value
	 */
	public boolean getValue() {
		return value;
	}

	/**
	 * @return the className
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @return the methodName
	 */
	public String getMethodName() {
		return methodName;
	}

	/**
	 * @return the lineNumber
	 */
	public int getLineNumber() {
		return lineNumber;
	}

	/**
	 * Determines whether this goals is connected to the given goal
	 * 
	 * This is the case when this goals target branch is control dependent on
	 * the target branch of the given goal or visa versa
	 * 
	 * This is used in the ChromosomeRecycler to determine if tests produced to
	 * cover one goal should be used initially when trying to cover the other
	 * goal
	 * 
	 * @param goal
	 *            a {@link org.evosuite.coverage.branch.BranchCoverageGoal}
	 *            object.
	 * @return a boolean.
	 */
	public boolean isConnectedTo(BranchCoverageGoal goal) {
		if (branch == null || goal.branch == null) {
			// one of the goals targets a root branch
			return goal.methodName.equals(methodName) && goal.className.equals(className);
		}

		// TODO map this to new CDG !

		return branch.getInstruction().isDirectlyControlDependentOn(goal.branch)
		        || goal.branch.getInstruction().isDirectlyControlDependentOn(branch);
	}

	/**
	 * <p>
	 * getDistance
	 * </p>
	 * 
	 * @param result
	 *            a {@link org.evosuite.testcase.execution.ExecutionResult} object.
	 * @return a {@link org.evosuite.coverage.ControlFlowDistance} object.
	 */
	public ControlFlowDistance getDistance(ExecutionResult result) {

		ControlFlowDistance r = ControlFlowDistanceCalculator.getDistance(result, branch, value,
				className, methodName);
		return r;
	}

	/**
	 * 
	 * @return
	 */
	public int hashCodeWithoutValue() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (branch == null ? 0 : branch.getActualBranchId());
		result = prime * result
		        + (branch == null ? 0 : branch.getInstruction().getInstructionId());
		result = prime * result + className.hashCode();
		result = prime * result + methodName.hashCode();
		return result;
	}
	
	// inherited from Object

	/**
	 * {@inheritDoc}
	 * 
	 * Readable representation
	 */
	@Override
	public String toString() {
		String name = className + "." + methodName + ":";
		if (branch != null) {
			name += " " + branch.toString();
			if (value)
				name += " - true";
			else
				name += " - false";
		} else
			name += " root-Branch";

		return name;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (branch == null ? 0 : branch.getActualBranchId());
		result = prime * result
		        + (branch == null ? 0 : branch.getInstruction().getInstructionId());
		// TODO sure you want to call hashCode() on the cfg? doesn't that take
		// long?
		// Seems redundant -- GF
		/*
		result = prime
		        * result
		        + ((branch == null) ? 0
		                : branch.getInstruction().getActualCFG().hashCode());
		                */
		result = prime * result + className.hashCode();
		result = prime * result + methodName.hashCode();
		result = prime * result + (value ? 1231 : 1237);
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		BranchCoverageGoal other = (BranchCoverageGoal) obj;
		// are we both root goals?
		if (this.branch == null) {
			if (other.branch != null)
				return false;
			else
				// i don't have to check for value at this point, because if
				// branch is null we are talking about the root branch here
				return this.methodName.equals(other.methodName)
				        && this.className.equals(other.className);
		}
		// well i am not, if you are we are different
		if (other.branch == null)
			return false;

		// so we both have a branch to cover, let's look at that branch and the
		// way we want it to be evaluated
		if (!this.branch.equals(other.branch))
			return false;
		else {
			return this.value == other.value;
		}
	}

	@Override
	public int compareTo(BranchCoverageGoal o) {
		int diff = lineNumber - o.lineNumber;
		if(diff == 0) {
			return 0;
			// TODO: this code in some cases leads to the violation of the compare
			// contract. I still have to figure out why - mattia
//			// Branch can only be null if this is a branchless method
//			if(branch == null || o.getBranch() == null)
//				return 0;
//			
//			// If on the same line, order by appearance in bytecode
//			return branch.getActualBranchId() - o.getBranch().getActualBranchId();
		} else {
			return diff;
		}
	}

	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
		// Write/save additional fields
		if (branch != null)
			oos.writeInt(branch.getActualBranchId());
		else
			oos.writeInt(-1);
	}

	// assumes "static java.util.Date aDate;" declared
	private void readObject(ObjectInputStream ois) throws ClassNotFoundException,
	        IOException {
		ois.defaultReadObject();

		int branchId = ois.readInt();
		if (branchId >= 0)
			this.branch = BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getBranch(branchId);
		else
			this.branch = null;
	}

}
