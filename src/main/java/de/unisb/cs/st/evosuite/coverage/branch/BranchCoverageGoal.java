/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.coverage.branch;

import java.io.Serializable;

import de.unisb.cs.st.evosuite.cfg.ControlDependency;
import de.unisb.cs.st.evosuite.coverage.ControlFlowDistance;
import de.unisb.cs.st.evosuite.coverage.TestCoverageGoal;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;

/**
 * A single branch coverage goal Either true/false evaluation of a jump
 * condition, or a method entry
 * 
 * @author Gordon Fraser, Andre Mis
 * 
 */
public class BranchCoverageGoal extends TestCoverageGoal implements Serializable {

	private static final long serialVersionUID = 2962922303111452419L;
	Branch branch;
	boolean value;

	String className;
	String methodName;

	/**
	 * Can be used to create an arbitrary BranchCoverageGoal trying to cover the
	 * given Branch
	 * 
	 * If the given branch is null, this goal will try to cover the root branch
	 * of the method identified by the given name - meaning it will just try to
	 * call the method at hand
	 * 
	 * Otherwise this goal will try to reach the given branch and if value is
	 * true, make the branchInstruction jump and visa versa
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
			if (!branch.getMethodName().equals(methodName)
					|| !branch.getClassName().equals(className))
				throw new IllegalArgumentException(
						"expect explicitly given information about a branch to coincide with the information given by that branch");
		}
	}
	
	public BranchCoverageGoal(ControlDependency cd, String className, String methodName) {
		this(cd.getBranch(),cd.getBranchExpressionValue(),className, methodName);
	}

	/**
	 * Methods that have no branches don't need a cfg, so we just set the cfg to
	 * null
	 * 
	 * @param className
	 * @param methodName
	 */
	public BranchCoverageGoal(String className, String methodName) {
		this.branch = null;
		this.value = true;

		this.className = className;
		this.methodName = methodName;
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
	 */
	public boolean isConnectedTo(BranchCoverageGoal goal) {
		if (branch == null || goal.branch == null) {
			// one of the goals targets a root branch
			return goal.methodName.equals(methodName)
					&& goal.className.equals(className);
		}

		// TODO map this to new CDG !

		return branch.getInstruction()
				.isDirectlyControlDependentOn(goal.branch)
				|| goal.branch.getInstruction().isDirectlyControlDependentOn(
						branch);
	}

//	/**
//	 * Returns the number of branches
//	 */
//	public int getDifficulty() {
//		int r = 1;
//
//		// TODO map this to new CDG !
//
//		if (branch != null) {
//			r += branch.getInstruction().getCDGDepth();
//		}
//		return r;
//	}

	/**
	 * Determine if there is an existing test case covering this goal
	 * 
	 * @return
	 */
	@Override
	public boolean isCovered(TestChromosome test) {
		ExecutionResult result = runTest(test);
		ControlFlowDistance d = getDistance(result);
		if (d.getApproachLevel() == 0 && d.getBranchDistance() == 0.0)
			return true;
		else
			return false;
	}

	public ControlFlowDistance getDistance(ExecutionResult result) {

		ControlFlowDistance r = ControlFlowDistanceCalculator.getDistance(
				result, branch, value, className, methodName);

		return r;
	}

	// SAFETY BACKUP BEFORE REIMPLEMENTATION
	// private ControlFlowDistance getNonRootDistance(List<Integer> path,
	// List<Double> true_distances, List<Double> false_distances) {
	//
	// if(branch == null)
	// throw new
	// IllegalStateException("expect getNonRootDistance() to only be called if this goal's branch is not a root branch");
	//		
	// ActualControlFlowGraph cfg = branch.getActualCFG();
	//		
	// ControlFlowDistance d = new ControlFlowDistance();
	// int min_approach = cfg.getDiameter() + 1;
	//		
	// double min_dist = 0.0;
	// for (int i = 0; i < path.size(); i++) {
	// BytecodeInstruction v = cfg.getInstruction(path.get(i));
	// if (v != null) {
	// int approach = cfg.getDistance(v, branch);
	// //logger.debug("B: Path vertex "+i+" has approach: "+approach+" and branch distance "+distances.get(i));
	//
	// if (approach <= min_approach && approach >= 0) {
	// double branch_distance = 0.0;
	//
	// if (approach > 0)
	// branch_distance = true_distances.get(i) + false_distances.get(i);
	// else if (value)
	// branch_distance = true_distances.get(i);
	// else
	// branch_distance = false_distances.get(i);
	//
	// if (approach == min_approach)
	// min_dist = Math.min(min_dist, branch_distance);
	// else {
	// min_approach = approach;
	// min_dist = branch_distance;
	// }
	//
	// }
	// } else {
	// logger.info("Path vertex does not exist in graph");
	// }
	// }
	//
	// d.approach = min_approach;
	// d.branch = min_dist;
	//
	// return d;
	// }

	// inherited from Object

	/**
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ (branch == null ? 0 : branch.getActualBranchId());
		result = prime
				* result
				+ (branch == null ? 0 : branch.getInstruction()
						.getInstructionId());
		// TODO sure you want to call hashCode() on the cfg? doesn't that take
		// long?
		result = prime
				* result
				+ ((branch == null) ? 0 : branch.getInstruction()
						.getActualCFG().hashCode());
		result = prime * result + className.hashCode();
		result = prime * result + methodName.hashCode();
		result = prime * result + (value ? 1231 : 1237);
		return result;
	}

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

}
