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
