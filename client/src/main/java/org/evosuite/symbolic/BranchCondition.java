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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.evosuite.classpath.ResourceList;
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

	private final BranchCondition previousBranchCondition;

	private final Constraint<?> localConstraint;

	private final List<Constraint<?>> supportingConstraints;

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
	public BranchCondition(BranchCondition previousBranchCondition, String className,
	        String methodName, int branchIndex, Constraint<?> localConstraint,
	        List<Constraint<?>> supportingConstraints) {

		this.className = ResourceList.getClassNameFromResourcePath(className);
		this.methodName = methodName;
		this.branchIndex = branchIndex;

		this.previousBranchCondition = previousBranchCondition;

		this.localConstraint = localConstraint;
		this.supportingConstraints = supportingConstraints;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		String ret = "";
		for (Constraint<?> c : this.supportingConstraints) {
			ret += " " + c + "\n";
		}

		ret += this.localConstraint;
		return ret;
	}

	public String getClassName() {
		return className;
	}

	public int getInstructionIndex() {
		return branchIndex;
	}

	public String getFullName() {
		return className + "." + methodName;
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
		constraints.addAll(supportingConstraints);

		BranchCondition current = previousBranchCondition;
		while (current != null) {
			constraints.addAll(current.supportingConstraints);
			constraints.add(current.localConstraint);
			current = current.getPreviousBranchCondition();
		}
		return constraints;
	}

	/**
	 * Returns the constraint for actual branch
	 * 
	 * @return
	 */
	public Constraint<?> getLocalConstraint() {
		return localConstraint;
	}

	/**
	 * Returns a list of implicit constraints (nullity checks, zero division,
	 * index within bounds, negative size array length, etc.) collected before
	 * the current branch condtion and after the last symbolic branch condition
	 * 
	 * @return
	 */
	public List<Constraint<?>> getSupportingConstraints() {
		return supportingConstraints;
	}
}
