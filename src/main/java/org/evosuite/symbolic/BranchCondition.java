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
	private final int lineNumber;

	// @Deprecated
	// public final Set<Constraint<?>> reachingConstraints;

	private final Set<Constraint<?>> localConstraints;

	private final List<Constraint<?>> listOfLocalConstraints;
	private final BranchCondition previousBranchCondition;

	/**
	 * <p>
	 * Constructor for BranchCondition.
	 * </p>
	 * 
	 * @param previousBranchCondition
	 *            TODO
	 * @param reachingConstraints
	 *            a {@link java.util.Set} object.
	 * @param localConstraints
	 *            a {@link java.util.Set} object.
	 * @param ins
	 *            a {@link gov.nasa.jpf.jvm.bytecode.Instruction} object.
	 */
	public BranchCondition(BranchCondition previousBranchCondition,
			String className, String methodName, int lineNumber,
			List<Constraint<?>> localConstraints) {
		this.className = className;
		this.methodName = methodName;
		this.lineNumber = lineNumber;

		// this.reachingConstraints = reachingConstraints;
		this.localConstraints = new HashSet<Constraint<?>>(localConstraints);
		this.listOfLocalConstraints = localConstraints;
		this.previousBranchCondition = previousBranchCondition;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		String ret = "Branch condition with " // + reachingConstraints.size()
				// + " reaching constraints and "
				+ localConstraints.size() + " local constraints: ";
		for (Constraint<?> c : localConstraints) {
			ret += " " + c;
		}

		return ret;
	}

	public Set<Constraint<?>> getReachingConstraints() {
		HashSet<Constraint<?>> constraints = new HashSet<Constraint<?>>();
		if (previousBranchCondition != null) {
			constraints.addAll(previousBranchCondition.getLocalConstraints());
			constraints
					.addAll(previousBranchCondition.getReachingConstraints());
		}
		return constraints;
	}

	public String getClassName() {
		return className;
	}

	public int getInstructionIndex() {
		return lineNumber;
	}

	public String getFullName() {
		return className + methodName;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public List<Constraint<?>> listOfLocalConstraints() {
		return listOfLocalConstraints;
	}

	/**
	 * @return the localConstraints
	 */
	public Set<Constraint<?>> getLocalConstraints() {
		return localConstraints;
	}

	public BranchCondition getPreviousBranchCondition() {
		return previousBranchCondition;
	}
}
