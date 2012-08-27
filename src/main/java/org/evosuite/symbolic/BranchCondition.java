/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 * 
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.symbolic;

import gnu.trove.set.hash.THashSet;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.evosuite.symbolic.expr.Constraint;

/**
 * <p>
 * BranchCondition class.
 * </p>
 * 
 * @author Gordon Fraser
 */
public class BranchCondition {
	private final String className;
	private final String methodName;
	private final int branchIndex;

	// @Deprecated
	// public final Set<Constraint<?>> reachingConstraints;

	private final BranchCondition previousBranchCondition;

	/**
	 * <p>
	 * Constructor for BranchCondition.
	 * </p>
	 * 
	 * @param previousBranchCondition
	 *            TODO
	 * @param localConstraint
	 *            TODO
	 * @param supportingConstraints
	 *            a {@link java.util.Set} object.
	 * @param reachingConstraints
	 *            a {@link java.util.Set} object.
	 * @param ins
	 *            a {@link gov.nasa.jpf.jvm.bytecode.Instruction} object.
	 */
	public BranchCondition(BranchCondition previousBranchCondition,
			String className, String methodName, int branchIndex,
			Constraint<?> localConstraint,
			List<Constraint<?>> supportingConstraints) {

		this.className = className;
		this.methodName = methodName;
		this.branchIndex = branchIndex;

		this.previousBranchCondition = previousBranchCondition;

		this.local_constraint = localConstraint;
		this.supporting_constraints = supportingConstraints;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		String ret = "Branch condition with local constraint "
				+ this.local_constraint + " and supporting constraints: ";
		for (Constraint<?> c : this.supporting_constraints) {
			ret += " " + c;
		}

		return ret;
	}

	public String getClassName() {
		return className;
	}

	public int getInstructionIndex() {
		return branchIndex;
	}

	public String getFullName() {
		return className + methodName;
	}

	@Deprecated
	public int getLineNumber() {
		return branchIndex;
	}

	/**
	 * @return the localConstraints
	 */
	@Deprecated
	public Set<Constraint<?>> getLocalConstraints() {
		Set<Constraint<?>> retVal = new THashSet<Constraint<?>>();
		retVal.addAll(this.supporting_constraints);
		retVal.add(this.local_constraint);
		return retVal;
	}

	private BranchCondition getPreviousBranchCondition() {
		return previousBranchCondition;
	}

	/**
	 * Returns a set of all the reaching constraints
	 * 
	 * @return
	 */
	public Set<Constraint<?>> getReachingConstraints() {
		HashSet<Constraint<?>> constraints = new HashSet<Constraint<?>>();
		BranchCondition current = previousBranchCondition;
		while (current != null) {
			constraints.addAll(current.supporting_constraints);
			constraints.add(local_constraint);
			current = current.getPreviousBranchCondition();
		}
		return constraints;
	}

	private final Constraint<?> local_constraint;

	private final List<Constraint<?>> supporting_constraints;

	/**
	 * Returns the constraint for actual branch
	 * 
	 * @return
	 */
	public Constraint<?> getLocalConstraint() {
		return local_constraint;
	}

	/**
	 * Returns a list of implicit constraints (nullity checks, zero division,
	 * index within bounds, negative size array length, etc.) collected before
	 * the current branch condtion and after the last symbolic branch condition
	 * 
	 * @return
	 */
	public List<Constraint<?>> getSupportingConstraints() {
		return supporting_constraints;
	}
}
