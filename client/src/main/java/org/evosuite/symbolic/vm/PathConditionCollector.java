/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.symbolic.vm;

import java.util.LinkedList;
import java.util.List;

import org.evosuite.symbolic.BranchCondition;
import org.evosuite.symbolic.IfBranchCondition;
import org.evosuite.symbolic.SwitchBranchCondition;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.IntegerConstraint;

/**
 * Collects a path condition during concolic execution
 * 
 * @author galeotti
 * 
 */
public final class PathConditionCollector {

	private final List<BranchCondition> branchConditions = new LinkedList<BranchCondition>();

	private final LinkedList<Constraint<?>> currentSupportingConstraints = new LinkedList<Constraint<?>>();

	private static Constraint<?> normalizeConstraint(IntegerConstraint c) {
		return ConstraintNormalizer.normalize(c);
	}

	/**
	 * Add a supporting constraint to the current branch condition When the branch
	 * condition is currently added, then these supporting constraints will be added
	 * to the new branch condition
	 * 
	 * @param c
	 */
	public void appendSupportingConstraint(IntegerConstraint c) {
		Constraint<?> normalizedConstraint = normalizeConstraint(c);
		currentSupportingConstraints.add(normalizedConstraint);
	}

	/**
	 * Add a new constraint to a branch condition for a IF instruction
	 * 
	 * @param className
	 *            the class name where the branch is
	 * @param methName
	 *            the method where the branch is
	 * @param branchIndex
	 *            the branch index
	 * @param c
	 *            the constraint for the branch condition
	 */
	public void appendIfBranchCondition(String className, String methName, int branchIndex, boolean isTrueBranch,
			IntegerConstraint c) {

		Constraint<?> normalizedConstraint = normalizeConstraint(c);

		LinkedList<Constraint<?>> branch_supporting_constraints = new LinkedList<Constraint<?>>(
				currentSupportingConstraints);

		IfBranchCondition new_branch = new IfBranchCondition(className, methName, branchIndex, normalizedConstraint,
				branch_supporting_constraints, isTrueBranch);

		branchConditions.add(new_branch);

		currentSupportingConstraints.clear();
	}

	/**
	 * Appends a switch branch condition originated by a switch bytecode instruction
	 * that matched a certain goal
	 * 
	 * @param className
	 * @param methodName
	 * @param instructionIndex
	 * @param goal
	 * @param c
	 */
	public void appendSwitchBranchCondition(String className, String methodName, int instructionIndex,
			IntegerConstraint c, int goal) {

		Constraint<?> normalizedConstraint = normalizeConstraint(c);

		LinkedList<Constraint<?>> branch_supporting_constraints = new LinkedList<Constraint<?>>(
				currentSupportingConstraints);

		SwitchBranchCondition new_branch = new SwitchBranchCondition(className, methodName, instructionIndex,
				normalizedConstraint, branch_supporting_constraints, goal);

		branchConditions.add(new_branch);

		currentSupportingConstraints.clear();

	}

	/**
	 * Returns the collected list of branch conditions during concolic execution
	 * 
	 * @return
	 */
	public List<BranchCondition> getPathCondition() {
		return new LinkedList<BranchCondition>(branchConditions);
	}

	/**
	 * Appends a switch branch condition originated by the execution of a switch
	 * bytecode instruction that did not match any goal
	 * 
	 * @param className
	 * @param methodName
	 * @param instructionIndex
	 * @param c
	 */
	public void appendDefaultSwitchBranchCondition(String className, String methodName, int instructionIndex,
			IntegerConstraint c) {

		Constraint<?> normalizedConstraint = normalizeConstraint(c);

		LinkedList<Constraint<?>> branch_supporting_constraints = new LinkedList<Constraint<?>>(
				currentSupportingConstraints);

		SwitchBranchCondition new_branch = new SwitchBranchCondition(className, methodName, instructionIndex,
				normalizedConstraint, branch_supporting_constraints);

		branchConditions.add(new_branch);

		currentSupportingConstraints.clear();

	}

}
