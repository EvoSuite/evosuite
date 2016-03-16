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
package org.evosuite.symbolic.vm;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.evosuite.symbolic.BranchCondition;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.IntegerConstraint;

/**
 * 
 * @author galeotti
 * 
 */
public final class PathConstraint {

	private BranchCondition previousBranchCondition = null;

	private final Stack<BranchCondition> branchConditions = new Stack<BranchCondition>();

	private final LinkedList<Constraint<?>> currentSupportingConstraints = new LinkedList<Constraint<?>>();

	private Constraint<?> normalizeConstraint(IntegerConstraint c) {
		return ConstraintNormalizer.normalize(c);
	}

	public void pushSupportingConstraint(IntegerConstraint c) {

		Constraint<?> normalizedConstraint = normalizeConstraint(c);
		currentSupportingConstraints.add(normalizedConstraint);

	}

	public void pushBranchCondition(String className, String methName, int branchIndex,
	        IntegerConstraint c) {

		Constraint<?> normalizedConstraint = normalizeConstraint(c);

		LinkedList<Constraint<?>> branch_supporting_constraints = new LinkedList<Constraint<?>>(
		        currentSupportingConstraints);

		BranchCondition new_branch = new BranchCondition(previousBranchCondition,
		        className, methName, branchIndex, normalizedConstraint,
		        branch_supporting_constraints);

		previousBranchCondition = new_branch;

		branchConditions.push(new_branch);

		currentSupportingConstraints.clear();
	}

	public List<BranchCondition> getBranchConditions() {
		return new LinkedList<BranchCondition>(branchConditions);
	}

}
